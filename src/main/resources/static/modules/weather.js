function WeatherModule(config, container) {
    const location = config.location || 'Amsterdam';
    const units = config.units || 'metric';
    const apiKey = config.apiKey || '';
    
    container.innerHTML = `
        <div class="weather-module">
            <div class="weather-loading">Loading weather for ${location}...</div>
            <div class="weather-content" style="display: none;">
                <div class="weather-current">
                    <h4 id="weather-location">${location}</h4>
                    <div id="weather-temp" class="weather-temp">--°</div>
                    <div id="weather-desc" class="weather-desc">--</div>
                </div>
                <div id="weather-details" class="weather-details"></div>
            </div>
            <style>
                .weather-module { padding: 0.5rem; }
                .weather-loading { text-align: center; color: #999; padding: 2rem; }
                .weather-current { text-align: center; }
                .weather-temp { font-size: 3rem; font-weight: 300; margin: 0.5rem 0; }
                .weather-desc { text-transform: capitalize; color: #666; }
                .weather-details { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; margin-top: 1rem; font-size: 0.875rem; color: #666; }
                .weather-detail { display: flex; justify-content: space-between; }
            </style>
        </div>
    `;

    async function fetchWeather() {
        if (!apiKey) {
            showMockWeather();
            return;
        }
        
        try {
            const url = `https://api.openweathermap.org/data/2.5/weather?q=${encodeURIComponent(location)}&units=${units}&appid=${apiKey}`;
            const response = await fetch(url);
            if (!response.ok) throw new Error('Weather API error');
            const data = await response.json();
            renderWeather(data);
        } catch (e) {
            console.error('Weather fetch error:', e);
            showMockWeather();
        }
    }

    function showMockWeather() {
        const mockData = {
            name: "Fake data for: " + location,
            main: { temp: 22, humidity: 65, pressure: 1013 },
            weather: [{ description: 'partly cloudy', icon: '02d' }],
            wind: { speed: 3.5 }
        };
        renderWeather(mockData);
    }

    function renderWeather(data) {
        container.querySelector('.weather-loading').style.display = 'none';
        const content = container.querySelector('.weather-content');
        content.style.display = 'block';
        
        container.querySelector('#weather-location').textContent = data.name;
        container.querySelector('#weather-temp').textContent = `${Math.round(data.main.temp)}°${units === 'metric' ? 'C' : 'F'}`;
        container.querySelector('#weather-desc').textContent = data.weather[0].description;
        
        const details = container.querySelector('#weather-details');
        details.innerHTML = `
            <div class="weather-detail"><span>Humidity</span><span>${data.main.humidity}%</span></div>
            <div class="weather-detail"><span>Pressure</span><span>${data.main.pressure} hPa</span></div>
            <div class="weather-detail"><span>Wind</span><span>${data.wind.speed} ${units === 'metric' ? 'm/s' : 'mph'}</span></div>
        `;
    }

    fetchWeather();
    
    if (config.refreshInterval) {
        setInterval(fetchWeather, config.refreshInterval * 1000);
    }
}

// Auto-initialize in browser
if (typeof window !== 'undefined') {
    window.WeatherModule = WeatherModule;
}