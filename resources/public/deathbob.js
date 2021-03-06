

var lat = localStorage["lat"] || 41.0;
var lng = localStorage["lng"] || -73.0;
var myLatlng = new google.maps.LatLng(lat, lng);
var opts = {enableHighAccuracy: true, maximumAge: 4000, timeout: 5000}
var colors = ['blue', 'green', 'yellow', 'orange', 'purple'];
var color = colors[Math.floor(Math.random()*colors.length)];
var target;
var map;
var name;

function changeName(n){
  if(n === undefined){
    name = prompt("Who are you?", "nobody");
  }else{
    name = n
  }
  name = name.split(' ')[0]
  localStorage["name"] = name;
}

changeName(localStorage["name"]);


var markers = [];


function get_location(){
  watch = navigator.geolocation.watchPosition(show_map, handle_error, opts);
}

function submitAnyway(){
  console.log("Submitting anyway");
  navigator.geolocation.getCurrentPosition(show_map, handle_error, opts);
}

function handle_error(err){
  console.log(err);
  if (err.code == 1){
    // user said no
  }
}

function show_map(position){
  lat = position.coords.latitude;
  lng = position.coords.longitude;
  localStorage["lat"] = lat;
  localStorage["lng"] = lng;
  newLatLng = new google.maps.LatLng(lat, lng);
  console.log("inside show_map");

  mark_position_for_name(newLatLng, name, color);

  if (window.websocket.readyState == 1){
    console.log("sending data");
    sendData();
  }else{
    watchWebsocket();
  }
}

function sendData(){
  console.log("sending data");
  var foo = new Date();
  $("lastSent").html(foo);
  $("#status").html("Sending Data");


  var toSend = JSON.stringify({name: name, lat: lat, lng: lng, color: color, target: target});
  websocket.send(toSend);
}

function sendMessage(){
  console.log("sending message");
  message = $("#message").val();
  var toSend = JSON.stringify({name: name, lat: lat, lng: lng, message: message, color: color});
  websocket.send(toSend);
}


function mark_position_for_name(position, name, color){
  console.log("Inside mark_position_for_name")
  var idx = find_index_of_name_in_markers(name);
  if(idx === -1){
    // couldn't find existing marker, make a new one

    image = "http://www.google.com/intl/en_us/mapfiles/ms/micons/XXX-dot.png".replace("XXX", color);
    marker_options = {
      position: position,
      map: map,
      title: name,
      icon: image
    }
    var mark = new google.maps.Marker(marker_options)
    markers.push(mark);
  }else{
    markers[idx].setPosition(position);
  }
  mapBounds = map.getBounds();

// for some reason the "target" never seems to be inside the bounds.
// so it keeps calling this and extending the bounds until the call stack overflows.
// wtf :(
//  if(mapBounds.contains(position)){
//    // do nothing
//  }else{
//    mapBounds.extend(position);
//    map.fitBounds(mapBounds);
//  }
}

function find_index_of_name_in_markers(name){
  for(var i = 0; i < markers.length; i++){
    if(markers[i].title === name) return i;
  }
  return -1;
}

function watchWebsocket(){
  console.log("watching websocket");
  if(window.websocket === undefined){
    openWebsocket();
  }else if(window.websocket.readyState === 0){
    console.log("websocket is opening");
  }else if(window.websocket.readyState === 1){
    console.log("websocket is open hooray!");
  }else if(window.websocket.readyState === 2){
    console.log("websocket is closing");
  }else if(window.websocket.readyState === 3){
    console.log("websocket already closed");
    openWebsocket();
  }else{
    console.log("This should never happen...");
  }
};

function googleIt(str){
  // need to encode str and append it to following
  var customSearch = "https://www.googleapis.com/customsearch/v1?key=AIzaSyBMeQaETGjkZNtCwEMhdb_Mhs8mI-v05rw&cx=000707085635839138990:4pnbfnwmrka&q="
  // do a get against customSearch + encoded str

};

function openWebsocket(){
  var wsPort = $('#ws-port').html();
  var ws = "ws://" + location.hostname + ":" + wsPort + "/ws"
  console.log(ws);
  window.websocket = new WebSocket(ws)
  window.websocket.onmessage = function(message){
    try {
      var json = JSON.parse(message.data);
      console.log("Got data!");
      $("#status").html("Got Data");
      var foo = new Date();
      $("#lastReceived").html(foo);
      console.log(json);
      showPosition(json);
      console.log("showed position");
      showMessage(json);
      console.log("showed message");
      showTarget(json);
      console.log("showed target");
    } catch (e) {
      console.log("This doesn't look like a valid JSON: ", message);
      return;
    }
  }
};

function showPosition(json){
  var name = json.name;
  var clr = json.color;
  if ((clr !== undefined) && (clr !== "")){
    var pos = new google.maps.LatLng(json.lat, json.lng);
    mark_position_for_name(pos, name, clr);
  }
}

function showTarget(json){
  var target = json.target;
  if((target !== undefined) && (target !== "")){
    var pos = new google.maps.LatLng(target.lat, target.lng);
    mark_position_for_name(pos, target.name, 'red');
  }
}

function showMessage(json){
  var mess = json.message
  if((mess !== undefined) && (mess !== "")){
    var newMess = $("<li>" + json.name + ": " + mess + "</li>");
    image = "http://www.google.com/intl/en_us/mapfiles/ms/micons/XXX-dot.png".replace("XXX", json.color);
    var newImg = $("<img src='" + image + "'/>");
    newMess.prepend(newImg);
    if ($("#messages li").length > 5){
      $("#messages li").last().hide();
    }
    $("#messages").prepend(newMess);
  }
};

function markGivenLocation(){
  var address = $('#searcher').val();
  if(address !== ''){
    var geocoder = new google.maps.Geocoder();
    var bounds = map.getBounds();
    var query = {address: address, bounds: bounds};
    geocoder.geocode(query, function(results, status){
      if (status == google.maps.GeocoderStatus.OK) {
        map.setCenter(results[0].geometry.location);
        var marker = new google.maps.Marker({
          map: map,
          position: results[0].geometry.location
        });
        var lt, lg;
        lt = results[0].geometry.location.lat();
        lg = results[0].geometry.location.lng();

        target = {target: {name: "Target " + name, lat: lt, lng: lg}};
        var toSend = JSON.stringify(target);
        websocket.send(toSend);
      } else {
        alert("Geocode was not successful for the following reason: " + status);
      }
    });
  }
};

function initialize() {
  mapOptions = {
    center: myLatlng,
    zoom: 14,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };
  map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
  openWebsocket();
  get_location();
  // It appears that having getCurrentPosition and watchPosition in flight at the same
  //   time messes them both up.  So have to choose one or the other. :(
  //        intervalTimer = setInterval(submitAnyway, 10000);
  socketStatus = setInterval(function(){
    $('#socketStatus').html(window.websocket.readyState);
    $('#socketStatus').hide().fadeIn(1000);
  }, 5000);
};

google.maps.event.addDomListener(window, 'load', initialize);
