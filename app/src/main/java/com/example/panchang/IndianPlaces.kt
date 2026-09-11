package com.example.panchang

/**
 * Offline starter place database used by the Kundali place picker.
 *
 * The picker also uses Android Geocoder as a live-search fallback, so the user
 * can type places that are not present in this bundled list.
 */
data class BirthPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

object IndianPlaces {

    val all: List<BirthPlace> = listOf(
        BirthPlace("Hyderabad, Telangana", 17.3850, 78.4867),
        BirthPlace("Warangal, Telangana", 17.9689, 79.5941),
        BirthPlace("Karimnagar, Telangana", 18.4386, 79.1288),
        BirthPlace("Nizamabad, Telangana", 18.6725, 78.0941),
        BirthPlace("Khammam, Telangana", 17.2473, 80.1514),
        BirthPlace("Adilabad, Telangana", 19.6641, 78.5320),
        BirthPlace("Mahbubnagar, Telangana", 16.7488, 77.9856),
        BirthPlace("Suryapet, Telangana", 17.1405, 79.6200),
        BirthPlace("Nalgonda, Telangana", 17.0575, 79.2684),
        BirthPlace("Siddipet, Telangana", 18.1019, 78.8520),

        BirthPlace("Vijayawada, Andhra Pradesh", 16.5062, 80.6480),
        BirthPlace("Visakhapatnam, Andhra Pradesh", 17.6868, 83.2185),
        BirthPlace("Guntur, Andhra Pradesh", 16.3067, 80.4365),
        BirthPlace("Tirupati, Andhra Pradesh", 13.6288, 79.4192),
        BirthPlace("Nellore, Andhra Pradesh", 14.4426, 79.9865),
        BirthPlace("Kurnool, Andhra Pradesh", 15.8281, 78.0373),
        BirthPlace("Rajahmundry, Andhra Pradesh", 17.0005, 81.8040),
        BirthPlace("Kakinada, Andhra Pradesh", 16.9891, 82.2475),
        BirthPlace("Kadapa, Andhra Pradesh", 14.4673, 78.8242),
        BirthPlace("Anantapur, Andhra Pradesh", 14.6819, 77.6006),
        BirthPlace("Ongole, Andhra Pradesh", 15.5057, 80.0499),
        BirthPlace("Eluru, Andhra Pradesh", 16.7107, 81.0952),
        BirthPlace("Srikakulam, Andhra Pradesh", 18.2969, 83.8973),
        BirthPlace("Machilipatnam, Andhra Pradesh", 16.1875, 81.1389),

        BirthPlace("Bengaluru, Karnataka", 12.9716, 77.5946),
        BirthPlace("Mysuru, Karnataka", 12.2958, 76.6394),
        BirthPlace("Mangaluru, Karnataka", 12.9141, 74.8560),
        BirthPlace("Hubballi, Karnataka", 15.3647, 75.1240),
        BirthPlace("Belagavi, Karnataka", 15.8497, 74.4977),
        BirthPlace("Kalaburagi, Karnataka", 17.3297, 76.8343),
        BirthPlace("Ballari, Karnataka", 15.1394, 76.9214),
        BirthPlace("Shivamogga, Karnataka", 13.9299, 75.5681),

        BirthPlace("Chennai, Tamil Nadu", 13.0827, 80.2707),
        BirthPlace("Coimbatore, Tamil Nadu", 11.0168, 76.9558),
        BirthPlace("Madurai, Tamil Nadu", 9.9252, 78.1198),
        BirthPlace("Tiruchirappalli, Tamil Nadu", 10.7905, 78.7047),
        BirthPlace("Salem, Tamil Nadu", 11.6643, 78.1460),
        BirthPlace("Tirunelveli, Tamil Nadu", 8.7139, 77.7567),
        BirthPlace("Thanjavur, Tamil Nadu", 10.7870, 79.1378),
        BirthPlace("Vellore, Tamil Nadu", 12.9165, 79.1325),
        BirthPlace("Erode, Tamil Nadu", 11.3410, 77.7172),
        BirthPlace("Thoothukudi, Tamil Nadu", 8.7642, 78.1348),

        BirthPlace("Mumbai, Maharashtra", 19.0760, 72.8777),
        BirthPlace("Pune, Maharashtra", 18.5204, 73.8567),
        BirthPlace("Nagpur, Maharashtra", 21.1458, 79.0882),
        BirthPlace("Nashik, Maharashtra", 19.9975, 73.7898),
        BirthPlace("Aurangabad, Maharashtra", 19.8762, 75.3433),
        BirthPlace("Kolhapur, Maharashtra", 16.7050, 74.2433),

        BirthPlace("New Delhi, Delhi", 28.6139, 77.2090),
        BirthPlace("Jaipur, Rajasthan", 26.9124, 75.7873),
        BirthPlace("Jodhpur, Rajasthan", 26.2389, 73.0243),
        BirthPlace("Udaipur, Rajasthan", 24.5854, 73.7125),
        BirthPlace("Ahmedabad, Gujarat", 23.0225, 72.5714),
        BirthPlace("Surat, Gujarat", 21.1702, 72.8311),
        BirthPlace("Vadodara, Gujarat", 22.3072, 73.1812),
        BirthPlace("Rajkot, Gujarat", 22.3039, 70.8022),

        BirthPlace("Bhopal, Madhya Pradesh", 23.2599, 77.4126),
        BirthPlace("Indore, Madhya Pradesh", 22.7196, 75.8577),
        BirthPlace("Jabalpur, Madhya Pradesh", 23.1815, 79.9864),
        BirthPlace("Raipur, Chhattisgarh", 21.2514, 81.6296),
        BirthPlace("Bhubaneswar, Odisha", 20.2961, 85.8245),
        BirthPlace("Cuttack, Odisha", 20.4625, 85.8830),

        BirthPlace("Kolkata, West Bengal", 22.5726, 88.3639),
        BirthPlace("Patna, Bihar", 25.5941, 85.1376),
        BirthPlace("Ranchi, Jharkhand", 23.3441, 85.3096),
        BirthPlace("Guwahati, Assam", 26.1445, 91.7362),
        BirthPlace("Bhubaneswar, Odisha", 20.2961, 85.8245),
        BirthPlace("Bhopal, Madhya Pradesh", 23.2599, 77.4126),
        BirthPlace("Lucknow, Uttar Pradesh", 26.8467, 80.9462),
        BirthPlace("Kanpur, Uttar Pradesh", 26.4499, 80.3319),
        BirthPlace("Varanasi, Uttar Pradesh", 25.3176, 82.9739),
        BirthPlace("Prayagraj, Uttar Pradesh", 25.4358, 81.8463),
        BirthPlace("Agra, Uttar Pradesh", 27.1767, 78.0081),
        BirthPlace("Dehradun, Uttarakhand", 30.3165, 78.0322),
        BirthPlace("Chandigarh", 30.7333, 76.7794),
        BirthPlace("Amritsar, Punjab", 31.6340, 74.8723),
        BirthPlace("Ludhiana, Punjab", 30.9010, 75.8573),
        BirthPlace("Srinagar, Jammu and Kashmir", 34.0837, 74.7973),
        BirthPlace("Jammu, Jammu and Kashmir", 32.7266, 74.8570),
        BirthPlace("Shimla, Himachal Pradesh", 31.1048, 77.1734),
        BirthPlace("Panaji, Goa", 15.4909, 73.8278),
        BirthPlace("Thiruvananthapuram, Kerala", 8.5241, 76.9366),
        BirthPlace("Kochi, Kerala", 9.9312, 76.2673),
        BirthPlace("Kozhikode, Kerala", 11.2588, 75.7804)
    ).distinctBy { it.name }
}
