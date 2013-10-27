var map, db;

function initDBConnection() {
    db = new Firebase("https://touch.firebaseIO-demo.com/")
}

function initMap() {
    var mapOptions = {
	center: new google.maps.LatLng(-34.397, 150.644),
	zoom: 8,
	mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
}

function init() {
    initDBConnection();
    initMap();
}

$(document).ready(init);

