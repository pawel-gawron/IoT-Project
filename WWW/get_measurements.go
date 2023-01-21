package main

import (
	"encoding/json"
	"log"
	"net/http"

	"github.com/zeromq/goczmq"
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

	new_rq, err := goczmq.NewReq("tcp://localhost:5555")
	if err != nil {
		log.Fatal(err)
	}
	defer new_rq.Destroy()

	new_rq.SendFrame([]byte("get_meas"), goczmq.FlagNone)
	reply, err := new_rq.RecvMessage()
	if err != nil {
		message := "Cannot get color settings from LED matrix"
		log.Printf("%s: %s\n", message, err)
		http.Error(w, message, http.StatusServiceUnavailable)
		return
	}

	var measurements_list []float32
	json.Unmarshal(reply[0], &measurements_list)

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
