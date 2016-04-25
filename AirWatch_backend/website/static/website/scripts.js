var userInfo = "None";
var apiURL = 'https://' + window.location.hostname + '/api/';
var map;
var points = [];


// Used to get CSRF Token Cookie so django will allow us to send requests
function getCookie(name) {
    var cookieValue = null;
    if (document.cookie && document.cookie != '') {
        var cookies = document.cookie.split(';');
        for (var i = 0; i < cookies.length; i++) {
            var cookie = jQuery.trim(cookies[i]);
            if (cookie.substring(0, name.length + 1) == (name + '=')) {
                cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                break;
            }
        }
    }
    return cookieValue;
}

function inputSymptoms() {
    $("#input-symptoms").modal('show');
}

function showPosition(position) {
    latitude = String(position.coords.latitude);
    longitude = String(position.coords.longitude);
    console.log (latitude);
    console.log (longitude);
}

function submitSymptoms() {
    var cough = $('#cough').val();
    var shortnessOfBreath = $('#shortnessOfBreath').val();
    var wheezing = $('#wheezing').val();
    var sneezing = $('#sneezing').val();
    var nasalObstruction = $('#nasalObstruction').val();
    var itchyEyes = $('#itchyEyes').val();

    var latitude = '';
    var longitude = '';

    if (latitude === '') {
        latitude = '0';
    }
    if (longitude === '') {
        longitude = '0';
    }

    $.ajax({
        url: apiURL + 'user/',

        type: 'POST',

        data: {
            'cough': cough,
            'shortnessOfBreath': shortnessOfBreath,
            'wheezing': wheezing,
            'sneezing': sneezing,
            'nasalObstruction': nasalObstruction,
            'itchyEyes': itchyEyes,
            'latitude': latitude,
            'longitude': longitude,
        },

        success: function (json) {
            console.log(json);
        },

        error: function (xhr, errmsg, err) {
            console.log(xhr.status + ": " + xhr.responseText);
        }
    })
}

$(function(){
    $.ajaxSetup({
        headers: { "X-CSRFToken": getCookie("csrftoken")},

        timeout: 600000,
    });

    $.ajax({
        url : apiURL + 'user/', 

        type : "GET",

        success : function(json) {
            userInfo = json;

            _.each(userInfo, function (val, i) {
                if (i === 0) {
                    var entry = val;
                    entry['count'] = 1;
                    points.push(entry);
                } else {
                    var count = 0;
                    _.each(points, function(p) {
                        if ( (Math.abs(p['longitude'] - val['longitude']) <= 1) && (Math.abs(p['latitude'] - val['latitude']) <= 1) ) {
                            p['cough'] = p['cough'] + val['cough'];
                            p['shortnessOfBreath'] = p['shortnessOfBreath'] + val['shortnessOfBreath'];
                            p['wheezing'] = p['wheezing'] + val['wheezing'];
                            p['sneezing'] = p['sneezing'] + val['sneezing'];
                            p['nasalObstruction'] = p['nasalObstruction'] + val['nasalObstruction'];
                            p['itchyEyes'] = p['itchyEyes'] + val['itchyEyes'];
                            p['count'] = p['count'] + 1;
                            p['latitude'] = ((parseFloat(p['latitude'])*(p['count']-1) + parseFloat(val['latitude']))/p['count']).toString();
                            p['longitude'] = ((parseFloat(p['longitude'])*(p['count']-1) + parseFloat(val['longitude']))/p['count']).toString();
                            count = 1;
                        } 
                    });
                    if (count === 0) {
                        var entry = val;
                        entry['count'] = 1;
                        points.push(entry);
                    }
                }
            });

            _.each(points, function (p) {
                _.each(p, function(value, key, obj) { 
                    if ( (key !== 'country') && (key !== 'city') && (key !== 'count') && (key !== 'latitude') && (key !== 'longitude') ) {
                        obj[key] = value / obj['count']; 
                    }
                });
            })
        },

        error : function(xhr,errmsg,err) {
            console.log(xhr.status + ": " + xhr.responseText);
        }
    });


    $(".btn").click(function () {
        // Size of marker = how many people 
        var selected = this.id;
        var bubbles = [];
        var bubblesOptions = {
            popupTemplate: function(geo, data) {
                return '<div class="hoverinfo">City:' + data.city;
            }
        };
        var severity = ['#00FF00', '#33FF00', '#66FF00', '#99FF00', '#CCFF00', '#FFFF00', '#FFD400', '#FFAA00', '#FF7F00', '#FF5500', '#FF2A00', '#FF0000']
        var averageForArea = 0;

        _.each(points, function (val){
            bubbles.push({
                name: 'Not a bomb, but centered on Brazil',
                radius: val['count'],
                latitude: val['latitude'],
                longitude: val['longitude'],
                fillKey: 'severity' + Math.floor(val[selected]),
                city: val['city'],
            })
        });
        basic_choropleth.bubbles(bubbles, bubblesOptions);
    });
});
