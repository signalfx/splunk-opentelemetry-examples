export interface ProductTemplate {
  name: string;
  description: string;
  category: string;
  brand: string;
  specifications: string;
}

export const ASTRONOMY_PRODUCTS: ProductTemplate[] = [
  {
    name: 'Celestron NexStar 127SLT Telescope',
    description:
      'Computerized GoTo telescope with SkyAlign technology for quick, easy star-finding',
    category: 'Telescopes',
    brand: 'Celestron',
    specifications: 'Aperture: 127mm, Focal Length: 1500mm, Mount: Computerized Alt-Az',
  },
  {
    name: 'Orion SkyQuest XT8 Classic Dobsonian',
    description:
      '8-inch reflector delivering stunning deep-sky views at an exceptional value',
    category: 'Telescopes',
    brand: 'Orion',
    specifications: 'Aperture: 203mm, Focal Length: 1200mm, F-ratio: f/5.9',
  },
  {
    name: 'Sky-Watcher 10" Collapsible Dobsonian',
    description:
      'Collapsible truss design for easy transport with impressive light-gathering power',
    category: 'Telescopes',
    brand: 'Sky-Watcher',
    specifications: 'Aperture: 254mm, Focal Length: 1200mm, F-ratio: f/4.7',
  },
  {
    name: 'Celestron Omni 32mm Plössl Eyepiece',
    description:
      'Wide-field 4-element Plössl design with excellent edge sharpness for panoramic views',
    category: 'Eyepieces',
    brand: 'Celestron',
    specifications: 'Focal Length: 32mm, Apparent FOV: 52°, Eye Relief: 20mm',
  },
  {
    name: 'Orion 25mm Plössl Eyepiece',
    description:
      'Classic Plössl design delivering crisp, high-contrast views with comfortable eye relief',
    category: 'Eyepieces',
    brand: 'Orion',
    specifications: 'Focal Length: 25mm, Apparent FOV: 50°, Eye Relief: 17mm',
  },
  {
    name: 'Meade Super Plössl 9mm Eyepiece',
    description:
      'Premium multi-coated high-magnification eyepiece for detailed planetary observation',
    category: 'Eyepieces',
    brand: 'Meade',
    specifications: 'Focal Length: 9mm, Apparent FOV: 52°, Eye Relief: 6mm',
  },
  {
    name: 'Baader Moon and Skyglow Filter',
    description:
      'Reduces light pollution and enhances contrast on emission nebulae and star clusters',
    category: 'Accessories',
    brand: 'Baader Planetarium',
    specifications: 'Filter Type: Broadband, Thread: 1.25", Transmission: 90%',
  },
  {
    name: 'Celestron Polar Alignment Scope',
    description:
      'High-precision illuminated reticle scope for accurate polar alignment of EQ mounts',
    category: 'Accessories',
    brand: 'Celestron',
    specifications: 'Reticle: Illuminated, Magnification: 6x, Compatibility: Standard EQ mounts',
  },
  {
    name: 'Astrozap Flexible Dew Shield',
    description:
      'Neoprene dew shield prevents moisture build-up on corrector plates during long sessions',
    category: 'Accessories',
    brand: 'Astrozap',
    specifications: 'Material: Neoprene, Fits: 8" SCT, Weight: 120g',
  },
  {
    name: 'Turn Left at Orion — 5th Edition',
    description:
      "The definitive beginner's guide to finding and observing over 100 celestial objects",
    category: 'Books',
    brand: 'Cambridge University Press',
    specifications: 'Pages: 280, Format: Spiral-bound, Edition: 5th',
  },
  {
    name: 'NightWatch: A Practical Guide',
    description:
      'Comprehensive guide to viewing the universe featuring full-colour star charts',
    category: 'Books',
    brand: 'Firefly Books',
    specifications: 'Pages: 192, Format: Hardcover, Star Charts: Full-colour',
  },
  {
    name: "The Backyard Astronomer's Guide",
    description:
      'Essential reference covering equipment, sky tours, astrophotography, and more',
    category: 'Books',
    brand: 'Firefly Books',
    specifications: 'Pages: 352, Format: Hardcover, Edition: 3rd',
  },
  {
    name: 'Sky-Watcher EQ5 Equatorial Mount',
    description:
      'Sturdy manual equatorial mount ideal for visual observing and introductory imaging',
    category: 'Mounts',
    brand: 'Sky-Watcher',
    specifications: 'Payload: 10kg, RA Motor: Optional add-on, Dec Motor: Optional add-on',
  },
  {
    name: 'iOptron CEM25P GoTo EQ Mount',
    description:
      'Center-balanced equatorial mount with built-in GPS, Wi-Fi, and full GoTo capability',
    category: 'Mounts',
    brand: 'iOptron',
    specifications: 'Payload: 11kg, GoTo: Yes, GPS: Built-in, Connectivity: Wi-Fi',
  },
  {
    name: 'Celestron Advanced VX Mount',
    description:
      'Versatile computerized EQ mount suitable for both visual observing and astrophotography',
    category: 'Mounts',
    brand: 'Celestron',
    specifications: 'Payload: 13.6kg, GoTo: Yes, Guide Port: ST-4, Ports: 2x AUX',
  },
];
