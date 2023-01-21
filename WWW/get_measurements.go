package main

import (
	"encoding/json"
	"log"
	"net/http"
	"os/exec"
)

type Measurement struct {
	Value float32 `json:"value"`
	Unit  string  `json:"unit"`
}

func get_measurements(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/get_measurements" {
		http.Error(w, "404 not found", http.StatusNotFound)
		return
	}

	if r.Method != "GET" {
		http.Error(w, "Not supported method", http.StatusNotImplemented)
		return
	}

	w.Header().Set("Content-Type", "application/json")

	output, err := exec.Command("./get_measurements.py").Output()

	if err != nil {
		message := "Cannot get measurements"
		log.Printf("%s: %s\n", message, err)
		http.Error(w, message, http.StatusServiceUnavailable)
		return
	}

	var measurements_list []float32
	json.Unmarshal(output, &measurements_list)

	resp := make(map[string]Measurement)
	resp["temperature"] = Measurement{Value: measurements_list[0], Unit: "C"}
	resp["humidity"] = Measurement{Value: measurements_list[1], Unit: "%"}
	resp["pressure"] = Measurement{Value: measurements_list[2], Unit: "hPa"}
	resp["roll"] = Measurement{Value: measurements_list[3], Unit: "deg"}
	resp["pitch"] = Measurement{Value: measurements_list[4], Unit: "deg"}
	resp["yaw"] = Measurement{Value: measurements_list[5], Unit: "deg"}

	jsonResp, err := json.Marshal(resp)
	if err != nil {
		log.Printf("Error happened in JSON marshal. Err: %s\n", err)
	}

	w.Write(jsonResp)
}
