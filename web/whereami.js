var locs, map;

function markTrail(points, centerView) {
    points = points.val();
    _.each(points, function(pt) {
	var latLng = new google.maps.LatLng(pt.lat, pt.lon);
	var marker = new google.maps.MarkerWithLabel({
	    position: latLng,
	    title: pt.time,
	    labelContent: pt.time,
	    labelClass: "labels", // the CSS class for the label
	    labelInBackground: false
	});
	marker.setMap(map);
    });

    if (centerView && points.length > 0) {
	var last = points[points.length - 1];
	var newCenter = new google.maps.LatLng(last.lat, last.lon);
	map.setCenter(newCenter);
    }
}

function initMap() {
    var mapOptions = {
	center: new google.maps.LatLng(-34.397, 150.644),
	zoom: 8,
	mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var mapDiv = document.getElementById("map-canvas");
    map = new google.maps.Map(mapDiv, mapOptions);
    locs.on("value", function(points) {
	markTrail(points, true);
	locs.off("value", arguments.callee);
	locs.on("value", function(changedPoints) {
	    markTrail(changedPoints, false);
	});
    })
}

function init() {
    locs = new Firebase("https://touch.firebaseio.com/locations")
    initMap();
}

$(document).ready(init);

