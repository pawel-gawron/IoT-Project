<?php
/**
 ******************************************************************************
 * @file    FIR Example server mock/test_signal.php
 * @author  Adrian Wojcik
 * @version V1.1
 * @date    17-Apr-2021
 * @brief   Simple IoT server mock: harmonic signal
 ******************************************************************************
 */
 
  header("Content-Type: application/json");
  $x = 2*M_PI*intval($_GET['k'])/100;
  $minTemp = 10;
  $maxTemp = 25;
  $minPress = 950;
  $maxPress = 1100;
  // $Temp = rand($minTemp, $maxTemp);
  $Press = rand($minPress, $maxPress);
  $Temp = 10*sin(1*$x)+20*sin(5*$x)+5*sin(18*$x)+7*sin(20*$x);
  
  $JSON = array('temp' => $Temp, 'press' => $Press);
  
  $myJSON = json_encode($JSON);
  
  echo $myJSON;
  // echo $Press;
?>