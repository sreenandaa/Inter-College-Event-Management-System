/**
 * InterCollege - Core Client Application Controller
 * Handles REST API communications, client routing, geolocation distance calculation,
 * student & coordinator dashboards, event discovery, external registration redirects,
 * and smart notifications.
 */

// Application State
const state = {
  currentUser: null,
  activeView: 'landing',
  userCoords: { lat: 8.5241, lon: 76.9366, city: 'Thiruvananthapuram', isGps: false },
  colleges: [],
  cities: [],
  notifications: [],
  unreadNotifs: 0,
  activeCategory: 'all',
  activeDistance: null,
  activeDateFilter: 'all',
  activeSearchQuery: '',
  activeSort: 'date_asc',
  pendingDeleteId: null,
  currentModalEvent: null
};

// Available Categories
const CATEGORIES = [
  'Technical', 'Cultural', 'Talk Session', 'Treasure Hunt',
  'Public Speaking', 'Quiz', 'Makeathon', 'Workshop', 'Sports', 'Other'
];

// Fallback City Coordinates for Manual Search
const CITY_COORDS = {
  'thiruvananthapuram': { lat: 8.5241, lon: 76.9366, name: 'Thiruvananthapuram' },
  'trivandrum': { lat: 8.5241, lon: 76.9366, name: 'Thiruvananthapuram' },
  'kochi': { lat: 10.0284, lon: 76.3288, name: 'Kochi' },
  'cochin': { lat: 10.0284, lon: 76.3288, name: 'Kochi' },
  'kozhikode': { lat: 11.2588, lon: 75.7804, name: 'Kozhikode' },
  'calicut': { lat: 11.2588, lon: 75.7804, name: 'Kozhikode' },
  'thrissur': { lat: 10.5534, lon: 76.2223, name: 'Thrissur' },
  'kottayam': { lat: 9.5916, lon: 76.5222, name: 'Kottayam' },
  'kollam': { lat: 8.8932, lon: 76.6141, name: 'Kollam' },
  'palakkad': { lat: 10.7867, lon: 76.6548, name: 'Palakkad' },
  'kannur': { lat: 11.8745, lon: 75.3704, name: 'Kannur' },
  'bangalore': { lat: 12.9716, lon: 77.5946, name: 'Bangalore' }
};

// ==========================================================================
// Initialization & Authentication Bootstrapping
// ==========================================================================
document.addEventListener('DOMContentLoaded', async () => {
  initInterestsCheckboxes();
  await checkCurrentUser();
  await loadMetadata();
  initRouter();
  requestBrowserNotificationPermissionPolitely();
});

async function apiFetch(endpoint, options = {}) {
  try {
    const response = await fetch(endpoint, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {})
      }
    });

    const result = await response.json();
    if (!response.ok || (result && result.success === false)) {
      const errorMsg = (result && result.message) || 'An unexpected error occurred.';
      throw new Error(errorMsg);
    }
    return result.data;
  } catch (err) {
    console.error(`API Error on ${endpoint}:`, err);
    throw err;
  }
}

async function checkCurrentUser() {
  try {
    const user = await apiFetch('/api/auth/me');
    state.currentUser = user;
    if (user && user.location) {
      setCityLocation(user.location);
    }
  } catch (e) {
    state.currentUser = null;
  }
  updateNavbar();
}

async function loadMetadata() {
  try {
    const [colleges, cities] = await Promise.all([
      apiFetch('/api/events/colleges'),
      apiFetch('/api/events/cities')
    ]);
    state.colleges = colleges || [];
    state.cities = cities || [];
  } catch (e) {
    console.warn('Metadata fetch warning:', e);
  }
}

// ==========================================================================
// Navigation & Navbar UI
// ==========================================================================
function updateNavbar() {
  const navLinks = document.getElementById('nav-links');
  const navActions = document.getElementById('nav-actions');
  const searchBar = document.getElementById('nav-search-bar');

  if (!state.currentUser) {
    // Unauthenticated Navbar
    searchBar.style.display = 'none';
    navLinks.innerHTML = `
      <li><a class="nav-item ${state.activeView === 'landing' ? 'active' : ''}" onclick="navigateTo('landing')">Home</a></li>
      <li><a class="nav-item ${state.activeView === 'explore' ? 'active' : ''}" onclick="navigateTo('explore')">Explore Events</a></li>
      <li><a class="nav-item" onclick="openAuthModal('login')">Sign In</a></li>
    `;

    navActions.innerHTML = `
      <button class="btn btn-outline btn-sm" onclick="openAuthModal('login')">Login</button>
      <button class="btn btn-mint btn-sm" onclick="openAuthModal('register')">Create Account</button>
    `;
  } else if (state.currentUser.role === 'STUDENT') {
    // Student Navbar
    searchBar.style.display = 'flex';
    navLinks.innerHTML = `
      <li><a class="nav-item ${state.activeView === 'student-dashboard' ? 'active' : ''}" onclick="navigateTo('student-dashboard')">Home</a></li>
      <li><a class="nav-item ${state.activeView === 'explore' ? 'active' : ''}" onclick="navigateTo('explore')">Explore Events</a></li>
      <li><a class="nav-item" onclick="filterByDistanceQuick(25)">Nearby</a></li>
      <li><a class="nav-item" onclick="scrollToRecommended()">Recommended</a></li>
    `;

    navActions.innerHTML = `
      <button class="notif-bell-btn" onclick="toggleNotifications(true)" title="Notifications" aria-label="Notifications">
        🔔
        <span class="notif-badge" id="nav-notif-count" style="display: none;">0</span>
      </button>
      <div class="user-profile-btn" onclick="openProfileModal()">
        <div class="avatar">${state.currentUser.fullName.charAt(0)}</div>
        <span style="font-size: 0.9rem; font-weight: 600; color: var(--text-main);">${escapeHtml(state.currentUser.fullName.split(' ')[0])}</span>
      </div>
      <button class="btn btn-subtle btn-sm" onclick="handleLogout()" title="Sign Out">Logout</button>
    `;
    fetchNotificationCount();
  } else if (state.currentUser.role === 'COORDINATOR') {
    // Coordinator Navbar
    searchBar.style.display = 'flex';
    navLinks.innerHTML = `
      <li><a class="nav-item ${state.activeView === 'coordinator-dashboard' ? 'active' : ''}" onclick="navigateTo('coordinator-dashboard')">Dashboard</a></li>
      <li><a class="nav-item" onclick="openCreateEventModal()">+ Create Event</a></li>
      <li><a class="nav-item ${state.activeView === 'explore' ? 'active' : ''}" onclick="navigateTo('explore')">All Events</a></li>
    `;

    navActions.innerHTML = `
      <button class="notif-bell-btn" onclick="toggleNotifications(true)" title="Notifications" aria-label="Notifications">
        🔔
        <span class="notif-badge" id="nav-notif-count" style="display: none;">0</span>
      </button>
      <div class="user-profile-btn" onclick="openProfileModal()">
        <div class="avatar" style="background: var(--green-primary);">${state.currentUser.fullName.charAt(0)}</div>
        <span style="font-size: 0.85rem; font-weight: 600; color: var(--mint-light);">${escapeHtml(state.currentUser.fullName.split(' ')[0])} (Coord)</span>
      </div>
      <button class="btn btn-subtle btn-sm" onclick="handleLogout()" title="Sign Out">Logout</button>
    `;
    fetchNotificationCount();
  }
}

function toggleMobileNav() {
  const nav = document.getElementById('nav-links');
  nav.classList.toggle('mobile-open');
}

function navigateTo(viewName) {
  state.activeView = viewName;
  updateNavbar();

  const container = document.getElementById('main-content');
  window.scrollTo({ top: 0, behavior: 'smooth' });

  if (viewName === 'landing' || (!state.currentUser && viewName === 'home')) {
    renderLandingView(container);
  } else if (viewName === 'student-dashboard' || (state.currentUser?.role === 'STUDENT' && viewName === 'home')) {
    renderStudentDashboardView(container);
  } else if (viewName === 'coordinator-dashboard' || (state.currentUser?.role === 'COORDINATOR' && viewName === 'home')) {
    renderCoordinatorDashboardView(container);
  } else if (viewName === 'explore') {
    renderExploreView(container);
  }
}

function initRouter() {
  if (state.currentUser) {
    if (state.currentUser.role === 'COORDINATOR') {
      navigateTo('coordinator-dashboard');
    } else {
      navigateTo('student-dashboard');
    }
  } else {
    navigateTo('landing');
  }
}

// ==========================================================================
// View: Landing Page
// ==========================================================================
async function renderLandingView(container) {
  container.innerHTML = `
    <div class="landing-hero">
      <div class="hero-pill">
        <span>✨</span> Over 500+ Students Connected Across Colleges
      </div>
      <h1 class="hero-title">InterCollege Event Discovery</h1>
      <p class="hero-tagline">Discover. Participate. Connect.</p>
      <p class="hero-description">
        Discover exciting events, competitions, workshops and activities happening across colleges around you.
        Join technical hackathons, cultural festivals, sports leagues, and speaker sessions seamlessly.
      </p>

      <div class="hero-buttons">
        <button class="btn btn-mint btn-lg" onclick="openAuthModal('login')">Explore Now</button>
        <button class="btn btn-outline btn-lg" onclick="openAuthModal('register')">Create Free Account</button>
      </div>

      <div class="hero-stats-row">
        <div class="hero-stat-card">
          <div class="stat-icon">🎓</div>
          <div class="stat-title">Multiple Colleges</div>
          <div class="stat-desc">Connect with prestigious engineering, arts, and medical campuses across regions.</div>
        </div>
        <div class="hero-stat-card">
          <div class="stat-icon">📍</div>
          <div class="stat-title">Location-Based Radius</div>
          <div class="stat-desc">Find events happening within 5km, 10km, or 50km of your current college location.</div>
        </div>
        <div class="hero-stat-card">
          <div class="stat-icon">⚡</div>
          <div class="stat-title">Real-Time Urgency</div>
          <div class="stat-desc">Smart status tracking highlights what is happening today, tomorrow, or this week.</div>
        </div>
      </div>
    </div>

    <!-- Featured Spotlight Events -->
    <div style="margin-top: 3rem;">
      <div class="section-header">
        <h2 class="section-title">🔥 Featured Upcoming Events</h2>
        <a class="view-all-link" onclick="navigateTo('explore')">Explore All Events →</a>
      </div>
      <div class="events-grid" id="landing-featured-grid">
        <div class="skeleton" style="height: 340px;"></div>
        <div class="skeleton" style="height: 340px;"></div>
        <div class="skeleton" style="height: 340px;"></div>
      </div>
    </div>
  `;

  // Fetch sample upcoming events for landing spotlight
  try {
    const events = await apiFetch('/api/events?sortBy=date_asc');
    const featuredGrid = document.getElementById('landing-featured-grid');
    if (featuredGrid) {
      featuredGrid.innerHTML = events.slice(0, 3).map(renderEventCard).join('');
    }
  } catch (e) {
    console.error('Failed to load landing events', e);
  }
}

// ==========================================================================
// View: Student Dashboard
// ==========================================================================
async function renderStudentDashboardView(container) {
  const user = state.currentUser;
  const firstName = user ? user.fullName.split(' ')[0] : 'Student';
  const cityName = state.userCoords.city || user?.location || 'Thiruvananthapuram';

  container.innerHTML = `
    <!-- Greeting & Location Banner -->
    <div class="dashboard-header">
      <div class="dashboard-greeting">
        <h1>Hello, ${escapeHtml(firstName)} 👋</h1>
        <p>Discover what's happening around you and connect with other campuses.</p>
      </div>

      <!-- Current Location & GPS Trigger -->
      <div class="location-card">
        <div class="location-info">
          <span class="location-label">Current City / Location</span>
          <span class="location-value" id="current-location-text">📍 ${escapeHtml(cityName)}</span>
        </div>
        <div style="display: flex; gap: 0.4rem;">
          <button class="btn btn-mint btn-sm" onclick="requestUserLocation()" title="Detect GPS location">
            ${state.userCoords.isGps ? '✓ GPS Active' : '📍 Use My Location'}
          </button>
          <button class="btn btn-subtle btn-sm" onclick="promptManualLocation()" title="Change city manually">
            Change
          </button>
        </div>
      </div>
    </div>

    <!-- Metric Summary Cards -->
    <div class="summary-cards-grid">
      <div class="summary-card" onclick="filterByUrgency('soon')">
        <div class="summary-icon">🔥</div>
        <div class="summary-info">
          <h3 id="stat-soon-count">--</h3>
          <p>Happening Soon</p>
        </div>
      </div>

      <div class="summary-card" onclick="filterByDistanceQuick(25)">
        <div class="summary-icon">📍</div>
        <div class="summary-info">
          <h3 id="stat-nearby-count">--</h3>
          <p>Near You (&lt; 25km)</p>
        </div>
      </div>

      <div class="summary-card" onclick="scrollToRecommended()">
        <div class="summary-icon">✨</div>
        <div class="summary-info">
          <h3 id="stat-recommended-count">--</h3>
          <p>Recommended For You</p>
        </div>
      </div>
    </div>

    <!-- Section: Happening Soon (Urgency) -->
    <div style="margin-bottom: 3.5rem;">
      <div class="section-header">
        <h2 class="section-title">
          🔥 Happening Soon
          <span class="badge badge-soon">Urgent</span>
        </h2>
        <a class="view-all-link" onclick="filterByUrgency('soon')">View All →</a>
      </div>
      <div class="events-grid" id="student-soon-grid">
        <div class="skeleton" style="height: 320px;"></div>
        <div class="skeleton" style="height: 320px;"></div>
      </div>
    </div>

    <!-- Section: Recommended For You (Student Interests) -->
    <div id="recommended-section" style="margin-bottom: 3.5rem;">
      <div class="section-header">
        <h2 class="section-title">
          ✨ Recommended For You
          <span class="badge badge-cat">Based on Interests</span>
        </h2>
        <a class="view-all-link" onclick="openProfileModal()">Edit Interests ✎</a>
      </div>
      <div class="events-grid" id="student-recommended-grid">
        <div class="skeleton" style="height: 320px;"></div>
        <div class="skeleton" style="height: 320px;"></div>
      </div>
    </div>

    <!-- Section: Nearby Events (Haversine Distance) -->
    <div style="margin-bottom: 3rem;">
      <div class="section-header">
        <h2 class="section-title">
          📍 Nearby Events in Your Region
          <span class="badge badge-upcoming">Proximity</span>
        </h2>
        <a class="view-all-link" onclick="filterByDistanceQuick(50)">Explore Nearby →</a>
      </div>
      <div class="events-grid" id="student-nearby-grid">
        <div class="skeleton" style="height: 320px;"></div>
        <div class="skeleton" style="height: 320px;"></div>
      </div>
    </div>
  `;

  loadStudentDashboardData();
}

async function loadStudentDashboardData() {
  const { lat, lon } = state.userCoords;

  try {
    const [soonEvents, recommendedEvents, nearbyEvents] = await Promise.all([
      apiFetch(`/api/events/happening-soon?userLat=${lat}&userLon=${lon}`),
      apiFetch(`/api/events/recommended?userLat=${lat}&userLon=${lon}`),
      apiFetch(`/api/events/nearby?userLat=${lat}&userLon=${lon}&maxDistanceKm=50`)
    ]);

    // Update summary counts
    document.getElementById('stat-soon-count').innerText = `${soonEvents.length} Events`;
    document.getElementById('stat-recommended-count').innerText = `${recommendedEvents.length} Events`;
    document.getElementById('stat-nearby-count').innerText = `${nearbyEvents.length} Events`;

    // Render Soon Grid
    const soonContainer = document.getElementById('student-soon-grid');
    if (soonContainer) {
      soonContainer.innerHTML = soonEvents.length
        ? soonEvents.slice(0, 3).map(renderEventCard).join('')
        : `<div class="empty-state" style="grid-column: 1/-1;">
            <div class="empty-icon">📅</div>
            <div class="empty-title">No events happening right away</div>
            <div class="empty-desc">Check the Explore page for all upcoming events later this month.</div>
          </div>`;
    }

    // Render Recommended Grid
    const recContainer = document.getElementById('student-recommended-grid');
    if (recContainer) {
      recContainer.innerHTML = recommendedEvents.length
        ? recommendedEvents.slice(0, 3).map(renderEventCard).join('')
        : `<div class="empty-state" style="grid-column: 1/-1;">
            <div class="empty-icon">🎯</div>
            <div class="empty-title">No recommendations yet</div>
            <div class="empty-desc">Set your interests in your profile to get personalized recommendations!</div>
            <button class="btn btn-mint btn-sm" onclick="openProfileModal()">Set Interests</button>
          </div>`;
    }

    // Render Nearby Grid
    const nearbyContainer = document.getElementById('student-nearby-grid');
    if (nearbyContainer) {
      nearbyContainer.innerHTML = nearbyEvents.length
        ? nearbyEvents.slice(0, 3).map(renderEventCard).join('')
        : `<div class="empty-state" style="grid-column: 1/-1;">
            <div class="empty-icon">📍</div>
            <div class="empty-title">No events within 50 km</div>
            <div class="empty-desc">Try expanding your distance filter or selecting another city.</div>
          </div>`;
    }
  } catch (err) {
    showToast('Failed to load dashboard events: ' + err.message, 'error');
  }
}

// ==========================================================================
// View: Coordinator Dashboard
// ==========================================================================
async function renderCoordinatorDashboardView(container) {
  const user = state.currentUser;
  const college = user ? user.college : 'Your College';

  container.innerHTML = `
    <!-- Coordinator Welcome Banner -->
    <div class="coordinator-banner">
      <div>
        <h1 style="font-size: 2rem; font-weight: 700;">Coordinator Portal</h1>
        <p style="color: var(--text-muted); margin-top: 0.3rem;">Manage events hosted by your institution.</p>
        <div class="college-badge-large">
          🏛️ Assigned Institution: <strong>${escapeHtml(college)}</strong>
        </div>
      </div>
      <div>
        <button class="btn btn-mint btn-lg" onclick="openCreateEventModal()">
          + Create New Event
        </button>
      </div>
    </div>

    <!-- Coordinator Metric Cards -->
    <div class="summary-cards-grid">
      <div class="summary-card">
        <div class="summary-icon">📋</div>
        <div class="summary-info">
          <h3 id="coord-stat-total">--</h3>
          <p>My College Events</p>
        </div>
      </div>

      <div class="summary-card">
        <div class="summary-icon">🟢</div>
        <div class="summary-info">
          <h3 id="coord-stat-upcoming">--</h3>
          <p>Upcoming Events</p>
        </div>
      </div>

      <div class="summary-card">
        <div class="summary-icon">🟡</div>
        <div class="summary-info">
          <h3 id="coord-stat-soon">--</h3>
          <p>Happening Soon</p>
        </div>
      </div>

      <div class="summary-card">
        <div class="summary-icon">⚪</div>
        <div class="summary-info">
          <h3 id="coord-stat-past">--</h3>
          <p>Completed Events</p>
        </div>
      </div>
    </div>

    <!-- Events Management List -->
    <div class="section-header">
      <h2 class="section-title">Events Hosted by ${escapeHtml(college)}</h2>
    </div>

    <div class="coordinator-table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>Event Name</th>
            <th>Category</th>
            <th>Date &amp; Time</th>
            <th>Venue</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody id="coordinator-events-tbody">
          <tr><td colspan="6" style="text-align: center; padding: 2rem;">Loading college events...</td></tr>
        </tbody>
      </table>
    </div>
  `;

  loadCoordinatorEvents();
}

async function loadCoordinatorEvents() {
  try {
    const events = await apiFetch('/api/events/my-college');
    const tbody = document.getElementById('coordinator-events-tbody');

    // Update stats
    const total = events.length;
    const upcoming = events.filter(e => e.status === 'UPCOMING').length;
    const soon = events.filter(e => e.status === 'HAPPENING_SOON' || e.status === 'TODAY').length;
    const past = events.filter(e => e.status === 'COMPLETED').length;

    document.getElementById('coord-stat-total').innerText = total;
    document.getElementById('coord-stat-upcoming').innerText = upcoming;
    document.getElementById('coord-stat-soon').innerText = soon;
    document.getElementById('coord-stat-past').innerText = past;

    if (!events.length) {
      tbody.innerHTML = `
        <tr>
          <td colspan="6" style="text-align: center; padding: 3rem;">
            <div style="font-size: 2rem; margin-bottom: 0.5rem;">📅</div>
            <div style="font-weight: 600; font-size: 1.1rem; color: var(--text-main);">No events created yet</div>
            <div style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1rem;">
              Click "+ Create New Event" to publish an event for your college.
            </div>
            <button class="btn btn-mint btn-sm" onclick="openCreateEventModal()">+ Create Event</button>
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = events.map(e => `
      <tr>
        <td style="font-weight: 600; color: var(--text-main);">
          ${escapeHtml(e.name)}
        </td>
        <td><span class="badge badge-cat">${escapeHtml(e.category)}</span></td>
        <td>${e.date} (${e.startTime || '09:00'})</td>
        <td>${escapeHtml(e.venue)}, ${escapeHtml(e.city)}</td>
        <td>${getStatusBadgeHtml(e.status, e.urgencyLabel)}</td>
        <td>
          <div class="table-actions">
            <button class="btn btn-subtle btn-sm" onclick="viewEventDetails('${e.id}')">View</button>
            <button class="btn btn-subtle btn-sm" onclick="openEditEventModal('${e.id}')">Edit</button>
            <button class="btn btn-danger btn-sm" onclick="promptDeleteEvent('${e.id}', '${escapeJsString(e.name)}')">Delete</button>
          </div>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    showToast('Failed to load coordinator events: ' + err.message, 'error');
  }
}

// ==========================================================================
// View: Explore Events
// ==========================================================================
async function renderExploreView(container) {
  container.innerHTML = `
    <!-- Explore Header -->
    <div style="margin-bottom: 1.5rem;">
      <h1 style="font-size: 2.2rem; font-weight: 700;">Explore Events</h1>
      <p style="color: var(--text-muted);">Search, filter, and discover intercollege events happening across all colleges.</p>
    </div>

    <!-- Search & Filter Controls -->
    <div class="toolbar-card">
      <div class="search-row">
        <div class="search-input-wrap">
          <span class="search-icon-inside">🔍</span>
          <input type="text" id="explore-search-input" placeholder="Search event title, college, city, or topic..." value="${escapeHtml(state.activeSearchQuery)}" oninput="handleExploreSearchInput(event)">
        </div>

        <!-- College Filter -->
        <select id="explore-college-select" class="filter-select" onchange="handleFilterChange()">
          <option value="all">All Colleges</option>
          ${state.colleges.map(c => `<option value="${escapeHtml(c)}">${escapeHtml(c)}</option>`).join('')}
        </select>

        <!-- City Filter -->
        <select id="explore-city-select" class="filter-select" onchange="handleFilterChange()">
          <option value="all">All Cities</option>
          ${state.cities.map(ct => `<option value="${escapeHtml(ct)}">${escapeHtml(ct)}</option>`).join('')}
        </select>

        <!-- Distance Filter -->
        <select id="explore-distance-select" class="filter-select" onchange="handleFilterChange()">
          <option value="all">Any Distance</option>
          <option value="5">Within 5 km</option>
          <option value="10">Within 10 km</option>
          <option value="25">Within 25 km</option>
          <option value="50">Within 50 km</option>
        </select>

        <!-- Date Urgency Filter -->
        <select id="explore-date-select" class="filter-select" onchange="handleFilterChange()">
          <option value="all">Any Date</option>
          <option value="today">Today</option>
          <option value="tomorrow">Tomorrow</option>
          <option value="soon">Happening Soon (&le;3 days)</option>
          <option value="this_week">This Week (&le;7 days)</option>
          <option value="upcoming">All Upcoming</option>
          <option value="completed">Past Events</option>
        </select>

        <!-- Sorting -->
        <select id="explore-sort-select" class="filter-select" onchange="handleFilterChange()">
          <option value="date_asc">Sort: Date (Earliest First)</option>
          <option value="date_desc">Sort: Date (Latest First)</option>
          <option value="distance">Sort: Nearest Distance</option>
          <option value="name">Sort: Event Name (A-Z)</option>
        </select>
      </div>

      <!-- Category Filter Pills -->
      <div class="filter-chips-row">
        <span class="filter-chip ${state.activeCategory === 'all' ? 'active' : ''}" onclick="selectCategoryFilter('all')">
          All Categories
        </span>
        ${CATEGORIES.map(cat => `
          <span class="filter-chip ${state.activeCategory === cat ? 'active' : ''}" onclick="selectCategoryFilter('${cat}')">
            ${cat}
          </span>
        `).join('')}
      </div>
    </div>

    <!-- Active Filter Status & Results Counter -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
      <span id="explore-results-count" style="font-size: 0.95rem; color: var(--text-muted);">Showing events...</span>
      <button class="btn btn-subtle btn-sm" onclick="resetFilters()">Reset Filters</button>
    </div>

    <!-- Events Grid -->
    <div class="events-grid" id="explore-events-grid">
      <div class="skeleton" style="height: 340px;"></div>
      <div class="skeleton" style="height: 340px;"></div>
      <div class="skeleton" style="height: 340px;"></div>
    </div>
  `;

  loadExploreEvents();
}

let searchDebounceTimeout = null;
function handleExploreSearchInput(e) {
  clearTimeout(searchDebounceTimeout);
  searchDebounceTimeout = setTimeout(() => {
    state.activeSearchQuery = e.target.value;
    loadExploreEvents();
  }, 250);
}

function handleFilterChange() {
  const collegeSel = document.getElementById('explore-college-select');
  const citySel = document.getElementById('explore-city-select');
  const distSel = document.getElementById('explore-distance-select');
  const dateSel = document.getElementById('explore-date-select');
  const sortSel = document.getElementById('explore-sort-select');

  state.activeCollege = collegeSel ? collegeSel.value : 'all';
  state.activeCity = citySel ? citySel.value : 'all';
  state.activeDistance = distSel && distSel.value !== 'all' ? parseFloat(distSel.value) : null;
  state.activeDateFilter = dateSel ? dateSel.value : 'all';
  state.activeSort = sortSel ? sortSel.value : 'date_asc';

  loadExploreEvents();
}

function selectCategoryFilter(cat) {
  state.activeCategory = cat;
  const chips = document.querySelectorAll('.filter-chip');
  chips.forEach(c => {
    if (c.innerText.trim() === cat || (cat === 'all' && c.innerText.trim() === 'All Categories')) {
      c.classList.add('active');
    } else {
      c.classList.remove('active');
    }
  });
  loadExploreEvents();
}

function resetFilters() {
  state.activeSearchQuery = '';
  state.activeCategory = 'all';
  state.activeDistance = null;
  state.activeDateFilter = 'all';
  state.activeCollege = 'all';
  state.activeCity = 'all';
  state.activeSort = 'date_asc';
  renderExploreView(document.getElementById('main-content'));
}

async function loadExploreEvents() {
  const grid = document.getElementById('explore-events-grid');
  const countLabel = document.getElementById('explore-results-count');
  if (!grid) return;

  const { lat, lon } = state.userCoords;

  const params = new URLSearchParams();
  if (state.activeSearchQuery) params.set('query', state.activeSearchQuery);
  if (state.activeCollege && state.activeCollege !== 'all') params.set('college', state.activeCollege);
  if (state.activeCategory && state.activeCategory !== 'all') params.set('category', state.activeCategory);
  if (state.activeCity && state.activeCity !== 'all') params.set('city', state.activeCity);
  if (state.activeDateFilter && state.activeDateFilter !== 'all') params.set('dateFilter', state.activeDateFilter);
  if (state.activeDistance) params.set('maxDistanceKm', state.activeDistance);
  if (lat && lon) {
    params.set('userLat', lat);
    params.set('userLon', lon);
  }
  params.set('sortBy', state.activeSort);

  try {
    const events = await apiFetch(`/api/events?${params.toString()}`);
    if (countLabel) countLabel.innerText = `Showing ${events.length} event${events.length === 1 ? '' : 's'}`;

    if (!events.length) {
      grid.innerHTML = `
        <div class="empty-state" style="grid-column: 1/-1;">
          <div class="empty-icon">🔍</div>
          <div class="empty-title">No events found</div>
          <div class="empty-desc">Try changing your filters, clearing the search query, or expanding the distance radius.</div>
          <button class="btn btn-mint btn-sm" onclick="resetFilters()">Reset All Filters</button>
        </div>
      `;
      return;
    }

    grid.innerHTML = events.map(renderEventCard).join('');
  } catch (e) {
    showToast('Failed to load events: ' + e.message, 'error');
  }
}

// ==========================================================================
// Event Card Component Generator
// ==========================================================================
function renderEventCard(e) {
  const statusBadge = getStatusBadgeHtml(e.status, e.urgencyLabel);
  const distanceHtml = e.distanceKm !== null && e.distanceKm !== undefined
    ? `<span class="distance-chip">📍 ${e.distanceKm} km away</span>`
    : '';

  const posterSrc = e.posterImage || 'https://images.unsplash.com/photo-1511578314322-379afb476865?w=800&auto=format&fit=crop&q=80';

  return `
    <div class="event-card" onclick="viewEventDetails('${e.id}')">
      <div class="event-poster-wrap">
        <img class="event-poster" src="${escapeHtml(posterSrc)}" alt="${escapeHtml(e.name)}" loading="lazy">
        <div class="poster-overlay-top">
          <span class="badge badge-cat">${escapeHtml(e.category)}</span>
          ${statusBadge}
        </div>
      </div>
      <div class="event-card-body">
        <div class="event-college-name">
          🏛️ ${escapeHtml(e.college)}
        </div>
        <h3 class="event-title">${escapeHtml(e.name)}</h3>
        <p class="event-desc-snippet">${escapeHtml(e.description)}</p>

        <div class="event-meta-list">
          <div class="event-meta-item">
            <span class="meta-icon">📅</span>
            <span>${e.date} &nbsp;•&nbsp; ${e.startTime ? e.startTime : '09:00'}</span>
          </div>
          <div class="event-meta-item">
            <span class="meta-icon">📍</span>
            <span>${escapeHtml(e.venue)}, ${escapeHtml(e.city)}</span>
          </div>
        </div>

        <div class="event-card-footer">
          ${distanceHtml}
          <button class="btn btn-subtle btn-sm" style="margin-left: auto;" onclick="event.stopPropagation(); viewEventDetails('${e.id}')">
            View Details →
          </button>
        </div>
      </div>
    </div>
  `;
}

function getStatusBadgeHtml(status, label) {
  const text = label || status || 'Upcoming';
  switch (status) {
    case 'TODAY':
      return `<span class="badge badge-today">🔴 ${escapeHtml(text)}</span>`;
    case 'HAPPENING_SOON':
      return `<span class="badge badge-soon">🟡 ${escapeHtml(text)}</span>`;
    case 'ONGOING':
      return `<span class="badge badge-ongoing">🔵 ${escapeHtml(text)}</span>`;
    case 'COMPLETED':
      return `<span class="badge badge-completed">⚪ ${escapeHtml(text)}</span>`;
    case 'UPCOMING':
    default:
      return `<span class="badge badge-upcoming">🟢 ${escapeHtml(text)}</span>`;
  }
}

// ==========================================================================
// Event Details Modal & External Registration Redirect
// ==========================================================================
async function viewEventDetails(eventId) {
  const { lat, lon } = state.userCoords;
  try {
    const event = await apiFetch(`/api/events/${eventId}?userLat=${lat}&userLon=${lon}`);
    state.currentModalEvent = event;

    document.getElementById('modal-event-name').innerText = event.name;
    document.getElementById('modal-event-college').innerText = `🏛️ Hosted by ${event.college}`;
    document.getElementById('modal-event-desc').innerText = event.description;
    document.getElementById('modal-event-category').innerText = event.category;

    // Status Badge
    const statusEl = document.getElementById('modal-event-status');
    statusEl.outerHTML = getStatusBadgeHtml(event.status, event.urgencyLabel);

    // Poster
    const poster = document.getElementById('modal-event-poster');
    poster.src = event.posterImage || 'https://images.unsplash.com/photo-1511578314322-379afb476865?w=800&auto=format&fit=crop&q=80';

    // Date & Time
    document.getElementById('modal-event-datetime').innerText = `${event.date} • ${event.startTime || '09:00'} to ${event.endTime || '17:00'}`;

    // Venue & City
    document.getElementById('modal-event-venue').innerText = `${event.venue}, ${event.city}`;

    // Distance Badge
    const distEl = document.getElementById('modal-event-distance');
    if (event.distanceKm !== null && event.distanceKm !== undefined) {
      distEl.innerHTML = `<span class="distance-chip">📍 ${event.distanceKm} km from your current location</span>`;
      distEl.style.display = 'block';
    } else {
      distEl.style.display = 'none';
    }

    // Organizer & Contact
    document.getElementById('modal-event-contact').innerText =
      `${event.organizerName || 'College Coordinator'} (${event.contactInfo || 'Contact details available at venue'})`;

    // Max participants
    const maxWrap = document.getElementById('modal-event-max-wrap');
    if (event.maxParticipants) {
      document.getElementById('modal-event-max').innerText = `${event.maxParticipants} Seats`;
      maxWrap.style.display = 'block';
    } else {
      maxWrap.style.display = 'none';
    }

    openModal('event-details-modal');
  } catch (err) {
    showToast('Could not load event details: ' + err.message, 'error');
  }
}

/**
 * Validates and securely redirects the user to the external registration portal.
 * No user registration data is collected inside this application.
 */
function handleExternalRegistrationRedirect() {
  const event = state.currentModalEvent;
  if (!event || !event.registrationLink) {
    showToast('Registration link is missing for this event.', 'error');
    return;
  }

  const url = event.registrationLink.trim();
  if (!url.startsWith('http://') && !url.startsWith('https://')) {
    showToast('Invalid registration link format.', 'error');
    return;
  }

  showToast('Opening external registration portal...', 'success');
  // Open in new tab securely
  window.open(url, '_blank', 'noopener,noreferrer');
}

// ==========================================================================
// Coordinator Event Management (Create, Edit, Delete)
// ==========================================================================
function openCreateEventModal() {
  if (!state.currentUser || state.currentUser.role !== 'COORDINATOR') {
    showToast('Only coordinators can create events.', 'error');
    return;
  }

  document.getElementById('event-form-modal-title').innerText = 'Create New Event';
  document.getElementById('event-form-id').value = '';
  document.getElementById('event-crud-form').reset();

  // Enforce college lock
  const collegeInput = document.getElementById('event-form-college');
  collegeInput.value = state.currentUser.college;

  // Default date to today
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('event-form-date').value = today;

  openModal('event-form-modal');
}

async function openEditEventModal(eventId) {
  if (!state.currentUser || state.currentUser.role !== 'COORDINATOR') {
    showToast('Only coordinators can edit events.', 'error');
    return;
  }

  try {
    const event = await apiFetch(`/api/events/${eventId}`);

    // Frontend ownership check
    if (event.college.toLowerCase() !== state.currentUser.college.toLowerCase()) {
      showToast("You don't have permission to modify this event.", 'error');
      return;
    }

    document.getElementById('event-form-modal-title').innerText = 'Edit Event';
    document.getElementById('event-form-id').value = event.id;
    document.getElementById('event-form-college').value = event.college;
    document.getElementById('event-form-name').value = event.name;
    document.getElementById('event-form-desc').value = event.description;
    document.getElementById('event-form-category').value = event.category;
    document.getElementById('event-form-date').value = event.date;
    document.getElementById('event-form-start-time').value = event.startTime || '09:00';
    document.getElementById('event-form-end-time').value = event.endTime || '17:00';
    document.getElementById('event-form-venue').value = event.venue;
    document.getElementById('event-form-city').value = event.city;
    document.getElementById('event-form-reg-link').value = event.registrationLink;
    document.getElementById('event-form-contact').value = event.contactInfo || '';
    document.getElementById('event-form-max').value = event.maxParticipants || '';
    document.getElementById('event-form-poster').value = event.posterImage || '';

    openModal('event-form-modal');
  } catch (err) {
    showToast('Failed to load event: ' + err.message, 'error');
  }
}

async function handleEventFormSubmit(e) {
  e.preventDefault();

  const id = document.getElementById('event-form-id').value;
  const isEditing = !!id;

  const payload = {
    name: document.getElementById('event-form-name').value.trim(),
    description: document.getElementById('event-form-desc').value.trim(),
    category: document.getElementById('event-form-category').value,
    categories: [document.getElementById('event-form-category').value],
    date: document.getElementById('event-form-date').value,
    startTime: document.getElementById('event-form-start-time').value,
    endTime: document.getElementById('event-form-end-time').value,
    venue: document.getElementById('event-form-venue').value.trim(),
    city: document.getElementById('event-form-city').value.trim(),
    registrationLink: document.getElementById('event-form-reg-link').value.trim(),
    contactInfo: document.getElementById('event-form-contact').value.trim(),
    maxParticipants: document.getElementById('event-form-max').value ? parseInt(document.getElementById('event-form-max').value) : null,
    posterImage: document.getElementById('event-form-poster').value.trim() || null
  };

  const endpoint = isEditing ? `/api/events/${id}` : '/api/events';
  const method = isEditing ? 'PUT' : 'POST';

  try {
    await apiFetch(endpoint, {
      method: method,
      body: JSON.stringify(payload)
    });

    closeModal('event-form-modal');
    showToast(isEditing ? 'Event updated successfully!' : 'Event created successfully!', 'success');

    // Reload coordinator table if currently on coordinator view
    if (state.activeView === 'coordinator-dashboard') {
      loadCoordinatorEvents();
    } else {
      navigateTo('coordinator-dashboard');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function promptDeleteEvent(id, name) {
  state.pendingDeleteId = id;
  document.getElementById('delete-event-name').innerText = `"${name}"`;
  openModal('delete-confirm-modal');
}

async function executeEventDelete() {
  if (!state.pendingDeleteId) return;

  try {
    await apiFetch(`/api/events/${state.pendingDeleteId}`, { method: 'DELETE' });
    closeModal('delete-confirm-modal');
    showToast('Event deleted successfully.', 'success');
    state.pendingDeleteId = null;
    loadCoordinatorEvents();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ==========================================================================
// Geolocation & Distance Handling (Haversine)
// ==========================================================================
function requestUserLocation() {
  if (!navigator.geolocation) {
    showToast('Geolocation is not supported by your browser. You can select your city manually.', 'error');
    return;
  }

  showToast('Detecting location via GPS...', 'success');

  navigator.geolocation.getCurrentPosition(
    position => {
      const lat = position.coords.latitude;
      const lon = position.coords.longitude;
      state.userCoords = {
        lat: Math.round(lat * 10000) / 10000,
        lon: Math.round(lon * 10000) / 10000,
        city: 'Your GPS Location',
        isGps: true
      };

      const locText = document.getElementById('current-location-text');
      if (locText) locText.innerHTML = `📍 GPS (${state.userCoords.lat}, ${state.userCoords.lon})`;

      showToast('Location updated! Recalculating event distances.', 'success');

      if (state.activeView === 'student-dashboard') loadStudentDashboardData();
      if (state.activeView === 'explore') loadExploreEvents();
    },
    error => {
      console.warn('Geolocation denied or failed:', error);
      showToast('Unable to access your location. You can search for a location manually.', 'error');
    },
    { timeout: 10000, enableHighAccuracy: false }
  );
}

function promptManualLocation() {
  const cityNames = Object.keys(CITY_COORDS).map(k => CITY_COORDS[k].name);
  const uniqueCities = [...new Set(cityNames)].join(', ');

  const chosen = prompt(`Enter your city/region:\n(Available hubs: ${uniqueCities})`, state.userCoords.city);
  if (chosen && chosen.trim()) {
    setCityLocation(chosen.trim());
    showToast(`Location set to ${state.userCoords.city}.`, 'success');
    if (state.activeView === 'student-dashboard') loadStudentDashboardData();
    if (state.activeView === 'explore') loadExploreEvents();
  }
}

function setCityLocation(cityName) {
  const norm = cityName.toLowerCase().trim();
  const matched = CITY_COORDS[norm];
  if (matched) {
    state.userCoords = { lat: matched.lat, lon: matched.lon, city: matched.name, isGps: false };
  } else {
    // Default fallback
    state.userCoords = { lat: 8.5241, lon: 76.9366, city: cityName, isGps: false };
  }
  const locText = document.getElementById('current-location-text');
  if (locText) locText.innerHTML = `📍 ${escapeHtml(state.userCoords.city)}`;
}

// ==========================================================================
// Notifications & Web Push Alerts
// ==========================================================================
async function fetchNotificationCount() {
  try {
    const data = await apiFetch('/api/notifications/unread-count');
    state.unreadNotifs = data.unreadCount || 0;
    const badge = document.getElementById('nav-notif-count');
    const drawerBadge = document.getElementById('drawer-unread-count');

    if (badge) {
      badge.innerText = state.unreadNotifs;
      badge.style.display = state.unreadNotifs > 0 ? 'flex' : 'none';
    }
    if (drawerBadge) {
      drawerBadge.innerText = state.unreadNotifs;
    }
  } catch (e) {
    console.warn('Notification count error', e);
  }
}

async function toggleNotifications(show) {
  const drawer = document.getElementById('notif-drawer');
  if (show) {
    drawer.classList.add('active');
    await loadNotificationsList();
  } else {
    drawer.classList.remove('active');
  }
}

async function loadNotificationsList() {
  const list = document.getElementById('notif-list');
  try {
    const notifs = await apiFetch('/api/notifications');
    state.notifications = notifs;

    if (!notifs.length) {
      list.innerHTML = `
        <div class="empty-state" style="padding: 2rem 1rem;">
          <div class="empty-icon">🔔</div>
          <div class="empty-title">All caught up!</div>
          <div class="empty-desc">No new notifications right now.</div>
        </div>
      `;
      return;
    }

    list.innerHTML = notifs.map(n => `
      <div class="notif-item ${!n.read ? 'unread' : ''}" onclick="handleNotifClick('${n.id}', '${n.relatedEventId}')">
        <div class="notif-title">${escapeHtml(n.title)}</div>
        <div class="notif-msg">${escapeHtml(n.message)}</div>
        <div class="notif-time">${formatDatePretty(n.createdAt)}</div>
      </div>
    `).join('');
  } catch (err) {
    list.innerHTML = `<div style="padding: 1rem; color: #E55353;">Failed to load notifications.</div>`;
  }
}

async function handleNotifClick(notifId, relatedEventId) {
  try {
    await apiFetch(`/api/notifications/${notifId}/read`, { method: 'PUT' });
    fetchNotificationCount();
    if (relatedEventId) {
      toggleNotifications(false);
      viewEventDetails(relatedEventId);
    } else {
      loadNotificationsList();
    }
  } catch (e) {
    console.error(e);
  }
}

async function markAllNotificationsRead() {
  try {
    await apiFetch('/api/notifications/read-all', { method: 'PUT' });
    fetchNotificationCount();
    loadNotificationsList();
    showToast('All notifications marked as read.', 'success');
  } catch (e) {
    showToast('Error: ' + e.message, 'error');
  }
}

function requestBrowserNotificationPermissionPolitely() {
  if ('Notification' in window && Notification.permission === 'default') {
    // We do not spam with aggressive popups immediately on page load,
    // but provide a friendly console log or subtle prompt when appropriate.
  }
}

// ==========================================================================
// Authentication Forms & Handlers
// ==========================================================================
function openAuthModal(tab = 'login') {
  switchAuthTab(tab);
  openModal('auth-modal');
}

function switchAuthTab(tab) {
  const loginBtn = document.getElementById('tab-login-btn');
  const regBtn = document.getElementById('tab-register-btn');
  const loginForm = document.getElementById('login-form');
  const regForm = document.getElementById('register-form');
  const title = document.getElementById('auth-modal-title');

  if (tab === 'login') {
    loginBtn.className = 'btn btn-mint btn-sm';
    regBtn.className = 'btn btn-subtle btn-sm';
    loginForm.style.display = 'block';
    regForm.style.display = 'none';
    title.innerText = 'Welcome Back';
  } else {
    loginBtn.className = 'btn btn-subtle btn-sm';
    regBtn.className = 'btn btn-mint btn-sm';
    loginForm.style.display = 'none';
    regForm.style.display = 'block';
    title.innerText = 'Join InterCollege';
  }
}

function toggleRoleRegistrationFields(role) {
  const studentFields = document.getElementById('student-specific-fields');
  const locationInput = document.getElementById('reg-location');

  if (role === 'STUDENT') {
    studentFields.style.display = 'block';
    locationInput.required = true;
  } else {
    studentFields.style.display = 'none';
    locationInput.required = false;
  }
}

function quickFillLogin(username, password) {
  document.getElementById('login-username').value = username;
  document.getElementById('login-password').value = password;
}

async function handleLoginSubmit(e) {
  e.preventDefault();
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value;

  try {
    const user = await apiFetch('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });

    state.currentUser = user;
    closeModal('auth-modal');
    showToast(`Welcome back, ${user.fullName}!`, 'success');

    if (user.location) {
      setCityLocation(user.location);
    }

    if (user.role === 'COORDINATOR') {
      navigateTo('coordinator-dashboard');
    } else {
      navigateTo('student-dashboard');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleRegisterSubmit(e) {
  e.preventDefault();

  const role = document.querySelector('input[name="reg-role"]:checked').value;
  const fullName = document.getElementById('reg-fullname').value.trim();
  const username = document.getElementById('reg-username').value.trim();
  const password = document.getElementById('reg-password').value;
  const confirmPassword = document.getElementById('reg-confirm-password').value;

  let college = document.getElementById('reg-college').value;
  if (college === 'Other College') {
    college = document.getElementById('reg-custom-college').value.trim();
  }

  if (password !== confirmPassword) {
    showToast('Passwords do not match.', 'error');
    return;
  }

  try {
    let user;
    if (role === 'STUDENT') {
      const location = document.getElementById('reg-location').value.trim();
      const interests = Array.from(document.querySelectorAll('#reg-interests-grid input:checked')).map(cb => cb.value);

      user = await apiFetch('/api/auth/register/student', {
        method: 'POST',
        body: JSON.stringify({ fullName, username, password, confirmPassword, college, location, interests })
      });
    } else {
      user = await apiFetch('/api/auth/register/coordinator', {
        method: 'POST',
        body: JSON.stringify({ fullName, username, password, confirmPassword, college })
      });
    }

    state.currentUser = user;
    closeModal('auth-modal');
    showToast(`Account created successfully! Welcome, ${user.fullName}.`, 'success');

    if (user.role === 'COORDINATOR') {
      navigateTo('coordinator-dashboard');
    } else {
      navigateTo('student-dashboard');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleLogout() {
  try {
    await apiFetch('/api/auth/logout', { method: 'POST' });
  } catch (e) {
    console.warn(e);
  }
  state.currentUser = null;
  showToast('Logged out successfully.', 'success');
  navigateTo('landing');
}

// ==========================================================================
// Profile Management
// ==========================================================================
async function openProfileModal() {
  if (!state.currentUser) return;

  try {
    const profile = await apiFetch('/api/users/profile');
    document.getElementById('profile-avatar-display').innerText = profile.fullName.charAt(0);
    document.getElementById('profile-name-display').innerText = profile.fullName;
    document.getElementById('profile-role-display').innerText = `Role: ${profile.role}`;
    document.getElementById('profile-college-display').innerText = profile.college;

    document.getElementById('profile-fullname').value = profile.fullName;
    document.getElementById('profile-location').value = profile.location || '';

    const interestSection = document.getElementById('profile-interests-section');
    if (profile.role === 'STUDENT') {
      interestSection.style.display = 'block';
      const pGrid = document.getElementById('profile-interests-grid');
      pGrid.innerHTML = CATEGORIES.map(cat => {
        const checked = profile.interests && profile.interests.includes(cat) ? 'checked' : '';
        return `
          <label class="checkbox-label">
            <input type="checkbox" value="${cat}" ${checked}>
            ${cat}
          </label>
        `;
      }).join('');
    } else {
      interestSection.style.display = 'none';
    }

    openModal('profile-modal');
  } catch (err) {
    showToast('Failed to load profile: ' + err.message, 'error');
  }
}

async function handleProfileUpdateSubmit(e) {
  e.preventDefault();
  const fullName = document.getElementById('profile-fullname').value.trim();
  const location = document.getElementById('profile-location').value.trim();
  const interests = Array.from(document.querySelectorAll('#profile-interests-grid input:checked')).map(cb => cb.value);

  try {
    const updated = await apiFetch('/api/users/profile', {
      method: 'PUT',
      body: JSON.stringify({ fullName, location, interests })
    });

    state.currentUser = updated;
    closeModal('profile-modal');
    showToast('Profile updated successfully!', 'success');
    updateNavbar();
    if (state.activeView === 'student-dashboard') loadStudentDashboardData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ==========================================================================
// Quick Shortcuts & Helpers
// ==========================================================================
function filterByUrgency(urgency) {
  state.activeDateFilter = urgency;
  navigateTo('explore');
}

function filterByDistanceQuick(maxKm) {
  state.activeDistance = maxKm;
  navigateTo('explore');
}

function scrollToRecommended() {
  if (state.activeView !== 'student-dashboard') {
    navigateTo('student-dashboard');
    setTimeout(() => {
      const el = document.getElementById('recommended-section');
      if (el) el.scrollIntoView({ behavior: 'smooth' });
    }, 200);
  } else {
    const el = document.getElementById('recommended-section');
    if (el) el.scrollIntoView({ behavior: 'smooth' });
  }
}

function handleNavSearch(e) {
  if (e.key === 'Enter') {
    state.activeSearchQuery = e.target.value;
    navigateTo('explore');
  }
}

function initInterestsCheckboxes() {
  const regGrid = document.getElementById('reg-interests-grid');
  if (regGrid) {
    regGrid.innerHTML = CATEGORIES.map(cat => `
      <label class="checkbox-label">
        <input type="checkbox" value="${cat}">
        ${cat}
      </label>
    `).join('');
  }

  // Handle custom college dropdown
  const collegeSelect = document.getElementById('reg-college');
  const customInput = document.getElementById('reg-custom-college');
  if (collegeSelect && customInput) {
    collegeSelect.addEventListener('change', () => {
      customInput.style.display = collegeSelect.value === 'Other College' ? 'block' : 'none';
      customInput.required = collegeSelect.value === 'Other College';
    });
  }
}

function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('active');
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('active');
}

function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <span>${type === 'success' ? '✓' : '⚠️'}</span>
    <span style="flex: 1; font-size: 0.9rem;">${escapeHtml(message)}</span>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function escapeJsString(str) {
  if (!str) return '';
  return String(str).replace(/'/g, "\\'");
}

function formatDatePretty(isoString) {
  if (!isoString) return '';
  try {
    const d = new Date(isoString);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  } catch (e) {
    return isoString;
  }
}
