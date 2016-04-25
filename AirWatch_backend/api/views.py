from __future__ import division

from decimal import *
from rest_framework import permissions, renderers, status, viewsets
from rest_framework.decorators import api_view, detail_route, list_route
import random
import requests
from rest_framework.response import Response
from rest_framework.reverse import reverse
from rest_framework.views import APIView
from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator

from airCheck.models import User as appUser
from .serializers import UserSerializer


weather_api_key = "c92c080111e8f149c929c282b212f647"
air_quality_api_key = "8089052ef2764c588b20ae257700dc40"


class UserViewSet(viewsets.ModelViewSet):
    queryset = appUser.objects.all()
    serializer_class = UserSerializer

    @method_decorator(csrf_exempt)
    def dispatch(self, *args, **kwargs):
        return super(UserViewSet, self).dispatch(*args, **kwargs)

    @list_route(methods=['get'])
    def average(self, request):
        users = appUser.objects.all()

        lat = Decimal(request.GET['lat'])
        lon = Decimal(request.GET['lon'])

        cough = 0
        shortnessOfBreath = 0
        wheezing = 0
        sneezing = 0
        nasalObstruction = 0
        itchyEyes = 0
        count = 0

        for u in users:
            if abs(Decimal(u.latitude) - lat) <= 1 and abs(Decimal(u.longitude) - lon) <= 1:
                cough = cough + u.cough
                shortnessOfBreath = shortnessOfBreath + u.shortnessOfBreath
                wheezing = wheezing + u.wheezing
                sneezing = sneezing + u.sneezing
                nasalObstruction = nasalObstruction + u.nasalObstruction
                itchyEyes = itchyEyes + u.itchyEyes
                count = count + 1

        if count > 0:
            cough = round(cough / count, 2)
            shortnessOfBreath = round(shortnessOfBreath / count, 2)
            wheezing = round(wheezing / count, 2)
            sneezing = round(sneezing / count, 2)
            nasalObstruction = round(nasalObstruction / count, 2)
            itchyEyes = round(itchyEyes / count, 2)

        return Response({
            'cough': cough,
            'shortnessOfBreath': shortnessOfBreath,
            'wheezing': wheezing,
            'sneezing': sneezing,
            'nasalObstruction': nasalObstruction,
            'itchyEyes': itchyEyes,
        })


class LocalInfo(APIView):

    def get(self, request, lat, lon):
        return_dict = {}

        req = requests.get('http://api.openweathermap.org/data/2.5/weather', params={
            'lat': lat,
            'lon': lon,
            'appid': weather_api_key
        })
        req = req.json()

        if req['cod'] == 200:
            return_dict['temp'] = "{0:.2f}".format(req['main']['temp'] - 273)
            return_dict['humidity'] = req['main']['humidity']
        else:
            return_dict['temp'] = req['message']
            return_dict['humidity'] = req['message']

        req = requests.get('http://api.breezometer.com/baqi/', params={
            'lat': lat,
            'lon': lon,
            'key': air_quality_api_key
        })
        req = req.json()

        if 'error' not in req:
            return_dict['airQuality'] = str(req['breezometer_aqi'])
        else:
            return_dict['airQuality'] = req['error']['message']

        return Response(return_dict)

    @method_decorator(csrf_exempt)
    def dispatch(self, *args, **kwargs):
        return super(LocalInfo, self).dispatch(*args, **kwargs)


@api_view(('GET',))
def api_root(request, format=None):
    return Response({
        'user': reverse('user-list', request=request, format=format)
    })
