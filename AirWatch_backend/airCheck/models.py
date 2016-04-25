from __future__ import unicode_literals

from django.db import models


class User(models.Model):
    cough = models.IntegerField()
    shortnessOfBreath = models.IntegerField()
    wheezing = models.IntegerField()
    nasalObstruction = models.IntegerField()
    itchyEyes = models.IntegerField()
    sneezing = models.IntegerField()
    country = models.CharField(max_length=20, default="Canada")
    city = models.CharField(max_length=20, default="Toronto")
    latitude = models.CharField(max_length=10, default="30")
    longitude = models.CharField(max_length=10, default="-40")
