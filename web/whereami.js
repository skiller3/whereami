var locs, map;

function markTrail(points, centerView) {
    points = points.val();
    var prevLatLng;
    _.each(points, function(pt) {
	var latLng = new google.maps.LatLng(pt.latitude, pt.longitude);
	var marker = new MarkerWithLabel({
	    position: latLng,
	    title: pt.time,
	    labelAnchor: new google.maps.Point(29, 0),
	    labelContent: pt.time,
	    labelClass: "labels", // the CSS class for the label
	    labelInBackground: false,
	    labelStyle: {opacity: 0.75}
	});
	marker.setMap(map);

	if (prevLatLng != null) {
	    new google.maps.Polyline({
		path: [prevLatLng, latLng],
		strokeColor: "#3000FF",
		strokeOpacity: 0.25,
		strokeWeight: 5,
		map: map
	    });
	}

	prevLatLng = latLng;
    });

    if (centerView && points.length > 0) {
	var last = points[points.length - 1];
	var newCenter = new google.maps.LatLng(last.latitude, last.longitude);
	map.setCenter(newCenter);
    }
}

function initMap() {
    var mapOptions = {
	center: new google.maps.LatLng(42.3581, -71.0636),
	zoom: 15,
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

