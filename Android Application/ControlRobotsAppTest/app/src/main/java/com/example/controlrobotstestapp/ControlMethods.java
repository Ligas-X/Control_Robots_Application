package com.example.controlrobotstestapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class ControlMethods extends AppCompatActivity {
	ToggleButton firstToggle, secondToggle, thirdToggle;
	Button okButton;

	String[] newItem = { "1", "1", "1" }; // создаем массив, который будем передавать (для передачи трех байтов)

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control_methods);

		firstToggle = (ToggleButton) findViewById(R.id.firstToggleButton);
		secondToggle = (ToggleButton) findViewById(R.id.secondToggleButton);
		thirdToggle = (ToggleButton) findViewById(R.id.thirdToggleButton);
		okButton = (Button) findViewById(R.id.buttonOK);

		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// записываем в массив передаваемые данные
				if (firstToggle.isChecked()) {
					newItem[0] = "1";
				} else {
					newItem[0] = "0";
				}
				if (secondToggle.isChecked()) {
					newItem[1] = "1";
				} else {
					newItem[1] = "0";
				}
				if (thirdToggle.isChecked()) {
					newItem[2] = "1";
				} else {
					newItem[2] = "0";
				}

				// создаем намерение для передачи результата в главное активити
				Intent intent = new Intent();
				intent.putExtra("newItem", newItem); // записываем в намерение наш массив с данными
				setResult(RESULT_OK, intent); // задаем результат текущего активити
				finish(); // и завершаем текущее активити
			}
		});
	}
}
