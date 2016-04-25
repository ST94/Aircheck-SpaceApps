from django.conf.urls import patterns, include, url
from rest_framework.routers import DefaultRouter
from . import views

router = DefaultRouter()
router.register(r'user', views.UserViewSet)

urlpatterns = patterns(
    '',
    url(r'^', include(router.urls)),
    url(r'^info/search/(?P<name>[\w\-]+)$', views.LocalInfo.as_view()),
    url(r'^info/(?P<lat>[/(-?\d+\.\d+),(-?\d+\.\d+)/]+)/(?P<lon>[/(-?\d+\.\d+),(-?\d+\.\d+)/]+)$', views.LocalInfo.as_view()),
)
