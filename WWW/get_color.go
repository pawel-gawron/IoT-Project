package main

import (
	"log"
	"net/http"
	"os/exec"
)

func get_color(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/get_color" {
		http.Error(w, "404 not found", http.StatusNotFound)
		return
	}

	if r.Method != "GET" {
		http.Error(w, "Not supported method", http.StatusNotImplemented)
		return
	}

	w.Header().Set("Content-Type", "application/json")

	output, err := exec.Command("./get_color.py").Output()

	if err != nil {
		message := "Cannot get color settings from LED matrix"
		log.Print(message)
		http.Error(w, message, http.StatusServiceUnavailable)
		return
	}

	w.Write([]byte(output))
}
