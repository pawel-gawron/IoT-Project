package main

import (
	"fmt"
	"log"
	"net/http"
)

func main() {
	http.HandleFunc("/set_color", set_color)
	http.HandleFunc("/get_color", get_color)
	http.HandleFunc("/get_measurements", get_measurements)

	fmt.Println("Starting server at port 8080")

	if err := http.ListenAndServe(":8080", nil); err != nil {
		log.Fatal(err)
	}
}
