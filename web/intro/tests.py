from django.test import TestCase

# Create your tests here.

class TestCase(TestCase):
    def testAPI(self):
        response = self.client.get('/')
        self.assertEqual(response.status_code, 200)