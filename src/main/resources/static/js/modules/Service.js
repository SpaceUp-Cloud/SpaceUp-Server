function serviceExecute(name, option) {
    let url = '/services/execute/' + name + '/' + option;
    $.ajax({
        url: url,
        method: 'post',
    }).done(function (response) {
        M.toast({html: response, classes: 'blue'})
    })
}