var map, locs;

function initDBConnection() {
    locs = new Firebase("https://touch.firebaseio.com/locations")
}

function initMap() {
    var mapOptions = {
	center: new google.maps.LatLng(-34.397, 150.644),
	zoom: 8,
	mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
    locs.on("value", function(points) {
	_.each(points.val(), function(pt) {
	    var latLng = new google.maps.LatLng(pt.lat, pt.lon);
	    var marker = new google.maps.Marker({
		position: latLng,
		title: pt.time
	    });
	    marker.setMap(map);
	})
    })
}

function init() {
    initDBConnection();
    initMap();
}

$(document).ready(init);

