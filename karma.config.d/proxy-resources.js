config.files.push({
    pattern: __dirname + "/src/commonTest/resources/**",
    watched: false,
    included: false,
    served: true,
    nocache: false
});
config.set({
    "proxies": {
        "/": __dirname + "/"
    },
    "urlRoot": "/__karma__/"
});
