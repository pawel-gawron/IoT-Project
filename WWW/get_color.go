package main

import (
	"log"
	"net/http"

	"github.com/zeromq/goczmq"
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

	new_rq, err := goczmq.NewReq("tcp://localhost:5555")
	if err != nil {
		log.Fatal(err)
	}
	defer new_rq.Destroy()

	new_rq.SendFrame([]byte("get_color"), goczmq.FlagNone)
	reply, err := new_rq.RecvMessage()
	if err != nil {
		message := "Cannot get color settings from LED matrix"
		log.Printf("%s: %s\n", message, err)
		http.Error(w, message, http.StatusServiceUnavailable)
		return
	}

	w.Write(reply[0])
}
