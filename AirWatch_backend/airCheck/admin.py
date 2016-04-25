from django.contrib import admin
from .models import User

# Register your models here.

class UserAdmin(admin.ModelAdmin):
    list_display = ('id', 'cough', 'shortnessOfBreath', 'wheezing', 'sneezing', 'nasalObstruction', 'itchyEyes', 'country', 'city', 'latitude', 'longitude')


admin.site.register(User, UserAdmin)
