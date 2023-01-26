<?php

  header("Content-Type: application/json");
  //$x = 2*M_PI*intval($_GET['k'])/100;
  $minHumidity = 30;
  $maxHumidity = 60;
  $minAngle = 30;
  $maxAngle = 60;
  $minPress = 950;
  $maxPress = 1100;
  $minTemp = 22;
  $maxTemp = 28;
  // $Temp = rand($minTemp, $maxTemp);
  $Press = rand($minPress, $maxPress);
  $Hum = rand($minHumidity, $maxHumidity);
  $Angle = rand($minAngle, $maxAngle);
  $Angle1 = rand($minAngle-5, $maxAngle+5);
  $Angle2 = rand($minAngle+5, $maxAngle-5);
  $Temp = rand($minTemp, $maxTemp);
  //$Temp = 10*sin(1*$x)+20*sin(5*$x)+5*sin(18*$x)+7*sin(20*$x);

  $JSON = array(
    'Temperature' => array(
        'value' => $Temp,
        'unit' => 'C',
    ),
    'Humidity' => array(
        'value' => $Hum,
        'unit' => 'mbar',
    ),
    'Pressure' => array(
        'value' => $Press,
        'unit' => 'mbar',
    ),
    'Roll position' => array(
        'value' => $Angle,
        'unit' => 'deg',
    ),
    'Pitch position' => array(
        'value' => $Angle1,
        'unit' => 'deg',
    ),
    'Yaw position' => array(
        'value' => $Angle2,
        'unit' => 'deg',
    )
);


  $JSON = json_encode($JSON);
  echo $JSON;
?>
