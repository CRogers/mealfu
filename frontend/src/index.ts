declare const URL_BASE: string;

const time = fetch(URL_BASE + '/test', {
    method: 'GET',
})
    .then(response => {
        return response.text();})
    .then(text => {
        console.log(text);
        const newChild = document.createElement('div');
        newChild.textContent = text;

        document.body.appendChild(newChild);
    });