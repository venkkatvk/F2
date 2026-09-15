// File: services/FlashSaleGateway.js

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
const SSE_BASE_URL = process.env.NEXT_PUBLIC_SSE_URL || 'http://localhost:8080/telemetry/stream';

export const FlashSaleGateway = {

    /**
     * 1. The Standard Offering (POST Order)
     * Formats the user's intent into the strict JSON schema required by Spring Boot.
     */
    submitOrder: async (userId, productId, quantity, price) => {
        const payload = { userId, productId, quantity, price };

        try {
            const response = await fetch(`${API_BASE_URL}/orders`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.status === 429) {
                throw new Error("The heavens are congested (Rate Limited). Please wait.");
            }
            if (response.status === 409) {
                throw new Error("Item sold out. The vault is empty.");
            }
            if (!response.ok) {
                throw new Error("A mysterious void consumed your request.");
            }

            return await response.json();
        } catch (error) {
            console.error("[Gateway Error]", error.message);
            throw error; 
        }
    },

    /**
     * 2. The Divine Pulse (SSE Connection)
     * Opens the persistent telemetry stream and maps the incoming bytes to a callback.
     */
    connectToTelemetry: (onUpdateCallback, onErrorCallback) => {
        const eventSource = new EventSource(SSE_BASE_URL);

        eventSource.onopen = () => console.log("[Gateway] Connected to divine telemetry.");

        eventSource.addEventListener('telemetry', (event) => {
            try {
                const data = JSON.parse(event.data);
                onUpdateCallback(data); // Pass the data back to the UI state securely
            } catch (err) {
                console.error("Failed to translate the Herald's message.", err);
            }
        });

        eventSource.onerror = (error) => {
            onErrorCallback(error);
            // ZBT: Always sever the connection cleanly if the temple burns down.
            eventSource.close(); 
        };

        return eventSource; // Return the instance so the UI can close it when unmounting
    }
};
