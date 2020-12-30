/**
 * Helper class for Service messages, received via SSE
 */
export default class SseServiceHandler {

    constructor() {}

    serviceExecution(feedback) {
        popup(JSON.parse(feedback))
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