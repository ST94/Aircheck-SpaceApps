from rest_framework import serializers

from airCheck.models import User


class UserSerializer(serializers.ModelSerializer):

    class Meta:
        model = User
        fields = ('id', 'cough', 'shortnessOfBreath', 'wheezing', 'nasalObstruction', 'itchyEyes', 'sneezing', 'country', 'city', 'latitude', 'longitude')
