# -*- coding: utf-8 -*-
# Generated by Django 1.9.5 on 2016-04-24 03:18
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('airCheck', '0003_auto_20160423_1439'),
    ]

    operations = [
        migrations.AddField(
            model_name='user',
            name='sneezing',
            field=models.IntegerField(default=0),
            preserve_default=False,
        ),
    ]
