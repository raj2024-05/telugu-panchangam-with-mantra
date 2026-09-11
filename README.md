# తెలుగు పంచాంగం — Complete Project

ఇది పాత project మీద patch చేయకుండా స్వతంత్రంగా ఉపయోగించడానికి సిద్ధం చేసిన project.

## ఇందులో ఉన్నవి
- దిన పంచాంగం
- తిథి, నక్షత్రం, యోగం, కరణం ending-time calculation
- వర్జ్యం, దుర్ముహూర్తం
- తెలుగు క్యాలెండర్ / పండుగలు
- కుండలి, షోడశవర్గాలు
- మంత్రాలు: default list లేదు; user add/edit/delete
- మంత్ర notification
- వ్రత సేకరణ: default list లేదు; user add/edit/delete
- వ్రత notification
- reading screen: A− / A+ / 5 themes
- SharedPreferences ద్వారా local storage
- GitHub Actions ద్వారా Debug APK build

## GitHub
1. ఈ project contents ను కొత్త repositoryలో upload చేయండి.
2. `.github/workflows/main.yml` అలాగే ఉంచండి.
3. Actions → Build Debug APK → Run workflow.
4. `TeluguPanchangam-debug-apk` artifact download చేయండి.

## ముఖ్య గమనిక
Panchanga ending times astronomical calculation ద్వారా local sunrise instant నుంచి boundary search చేసి లెక్కించబడతాయి. ఒక reference Panchangతో cross-check చేసి అవసరమైతే location/timezone-specific validation చేయాలి.
