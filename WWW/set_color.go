package main

import (
	"log"
	"net/http"
	"os/exec"
)

type Color struct {
	R     uint8 `json:"R"`
	G     uint8 `json:"G"`
	B     uint8 `json:"B"`
	State bool  `json:"state"`
}

func set_color(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/set_color" {
		http.Error(w, "404 not found", http.StatusNotFound)
		return
	}

	if r.Method != "GET" {
		http.Error(w, "Not supported method", http.StatusNotImplemented)
		return
	}

	query := r.URL.Query()
	color_settings, present := query["color_settings"]

	if !present {
		http.Error(w, "No color_settings parameter", http.StatusUnprocessableEntity)
		return
	}

	err := exec.Command("./set_color.py", color_settings[0]).Run()

	if err != nil {
		message := "Cannot set color settings from LED matrix"
		log.Printf("%s: %s\n", message, err)
		http.Error(w, message, http.StatusServiceUnavailable)
		return
	}
}
