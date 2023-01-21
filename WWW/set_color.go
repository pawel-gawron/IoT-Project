package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/zeromq/goczmq"
)

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

	new_rq, err := goczmq.NewReq("tcp://localhost:5555")
	if err != nil {
		log.Fatal(err)
	}
	defer new_rq.Destroy()

	new_msg := fmt.Sprintf("set_color:%s;", color_settings[0])
	new_rq.SendFrame([]byte(new_msg), goczmq.FlagNone)
	_, err = new_rq.RecvMessage()
	if err != nil {
		message := "Cannot set color settings from LED matrix"
		log.Printf("%s: %s\n", message, err)
		http.Error(w, message, http.StatusServiceUnavailable)
		return
	}
}
