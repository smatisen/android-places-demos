/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.placesdemo;

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;

/**
 * Activity for testing {@link PlacesClient#findCurrentPlace(FindCurrentPlaceRequest)}.
 */
public class CurrentPlaceTestActivity extends AppCompatActivity {

  private PlacesClient placesClient;
  private TextView responseView;
  private FieldSelector fieldSelector;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.current_place_test_activity);

    // Retrieve a PlacesClient (previously initialized - see MainActivity)
    placesClient = Places.createClient(this);

    // Set view objects
    List<Place.Field> placeFields =
        FieldSelector.getPlaceFields(Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
    fieldSelector =
        new FieldSelector(
            findViewById(R.id.use_custom_fields),
            findViewById(R.id.custom_fields_list),
            placeFields);
    responseView = findViewById(R.id.response);
    setLoading(false);

    // Set listeners for programmatic Find Current Place
    findViewById(R.id.find_current_place_button).setOnClickListener((view) -> findCurrentPlace());
  }

  /**
   * Fetches a list of {@link PlaceLikelihood} instances that represent the Places the user is
   * most
   * likely to be at currently.
   */
  // ASwB complains about MissingPermission even when permission is checked directly
  // (i.e not in a separate checkPermission() method).
  @SuppressLint("MissingPermission")
  private void findCurrentPlace() {
    // ACCESS_WIFI_STATE is a normal permission, no need to check for it.
    if (checkPermission(ACCESS_FINE_LOCATION)) {
      findCurrentPlaceWithPermissions();
    }
  }

  /**
   * Fetches a list of {@link PlaceLikelihood} instances that represent the Places the user is
   * most
   * likely to be at currently.
   */
  @RequiresPermission(allOf = {ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE})
  private void findCurrentPlaceWithPermissions() {
    setLoading(true);

    FindCurrentPlaceRequest currentPlaceRequest =
        FindCurrentPlaceRequest.builder(getPlaceFields()).build();
    Task<FindCurrentPlaceResponse> currentPlaceTask =
        placesClient.findCurrentPlace(currentPlaceRequest);

    currentPlaceTask.addOnSuccessListener(
        (response) ->
            responseView.setText(StringUtil.stringify(response, isDisplayRawResultsChecked())));

    currentPlaceTask.addOnFailureListener(
        (exception) -> {
          exception.printStackTrace();
          responseView.setText(exception.getMessage());
        });

    currentPlaceTask.addOnCompleteListener(task -> setLoading(false));
  }

  //////////////////////////
  // Helper methods below //
  //////////////////////////

  private List<Place.Field> getPlaceFields() {
    if (((CheckBox) findViewById(R.id.use_custom_fields)).isChecked()) {
      return fieldSelector.getSelectedFields();
    } else {
      return fieldSelector.getAllFields();
    }
  }

  private boolean checkPermission(String permission) {
    boolean hasPermission =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    if (!hasPermission) {
      ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
    }
    return hasPermission;
  }

  private boolean isDisplayRawResultsChecked() {
    return ((CheckBox) findViewById(R.id.display_raw_results)).isChecked();
  }

  private void setLoading(boolean loading) {
    findViewById(R.id.loading).setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
  }
}