import React, { useState, useEffect } from 'react';
import { FlashSaleGateway } from '../services/FlashSaleGateway';

export default function LiveDashboard() {
    // 1. The Scoreboard (State)
    const [telemetry, setTelemetry] = useState({ activeThreads: 0, processed: 0 });
    const [connectionStatus, setConnectionStatus] = useState('CONNECTING...');

    // 2. The Worker (Effect Hook)
    useEffect(() => {
        // We ask the High Priest to connect to the divine stream
        const eventSource = FlashSaleGateway.connectToTelemetry(
            (data) => {
                // Update the scoreboard safely!
                setTelemetry(data);
                setConnectionStatus('ENGINE LIVE');
            },
            (error) => {
                setConnectionStatus('ENGINE OFFLINE');
            }
        );

        // 3. The Sacred Cleanup (Preventing Memory Leaks)
        // If this component unmounts (user navigates away), we MUST sever the connection.
        return () => {
            console.log("Severing earthly ties. Closing SSE stream.");
            eventSource.close();
        };
    }, []); // The empty array ensures we only build the stadium ONCE.

    return (
        <div className="p-6 max-w-lg mx-auto bg-white rounded-xl shadow-md space-y-4">
            <h1 className="text-2xl font-bold">Flash Sale Command Center</h1>
            <div className="flex justify-between items-center border-b pb-4">
                <span className="font-semibold">Backend Status:</span>
                <span className={`px-3 py-1 text-sm font-bold text-white rounded-full ${connectionStatus === 'ENGINE LIVE' ? 'bg-green-500' : 'bg-red-500'}`}>
                    {connectionStatus}
                </span>
            </div>
            
            <div className="grid grid-cols-2 gap-4 text-center mt-4">
                <div className="bg-gray-100 p-4 rounded-lg">
                    <p className="text-sm text-gray-500">Active Threads</p>
                    <p className="text-3xl font-extrabold text-blue-600">{telemetry.activeThreads}</p>
                </div>
                <div className="bg-gray-100 p-4 rounded-lg">
                    <p className="text-sm text-gray-500">Total Processed</p>
                    <p className="text-3xl font-extrabold text-green-600">{telemetry.processed}</p>
                </div>
            </div>
        </div>
    );
}
