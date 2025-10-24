CREATE TABLE weather_searches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city_name TEXT NOT NULL,
    country TEXT,
    searched_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE weather_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    search_id UUID REFERENCES weather_searches(id),
    temperature DOUBLE PRECISION,
    feels_like DOUBLE PRECISION,
    temp_min DOUBLE PRECISION,
    temp_max DOUBLE PRECISION,
    pressure INTEGER,
    humidity INTEGER,
    description TEXT,
    icon TEXT,
    wind_speed DOUBLE PRECISION
);
