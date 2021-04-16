export class WsServiceHandler {
    constructor() {
        this.resource = "/services";

        let protocol = window.location.protocol === "http:" ? "ws:" : "wss:";
        let port = window.location.port

        let url = protocol + "//" + window.location.hostname + ":" + port  + "/ws" + this.resource;

        this.ws = new WebSocket(url);
        this.services = [{}]
    }

    init() {
        this.ws.onmessage = event => this.handle(event);
    }

    handle(event) {
        let services = JSON.parse(event.data);
        console.debug(services);
        this.services = services;
    }

    getServices() {
        return this.services;
    }
}

/**
 * Execute a service option
 * @param name - service name
 * @param option - service option (e.g. start, stop or restart)
 */
export function serviceExecute(name, option) {
    let url = '/api/service/execute/' + name + '/' + option;
    $.ajax({
        url: url,
        method: 'post',
        beforeSend: function () {
            enableLoader(name)
        },
        complete: function () {
            disableLoader(name)
        }
    });
}