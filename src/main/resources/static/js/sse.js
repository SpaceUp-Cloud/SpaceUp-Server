let timeSSE = new EventSource("/time/events");

timeSSE.onmessage = function(event) {
    document.getElementById("time").innerHTML = event.data;
}

timeSSE.onerror = function(err) {
  console.error("EventSource failed:", err);
};