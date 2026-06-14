#!/bin/bash

CLIENT_ID="final-llm-bench"
API_BASE="http://localhost:8085/api"

echo "Starting final benchmark for Client ID: $CLIENT_ID"

# 0. Count moves before starting
MOVES_BEFORE=$(docker compose logs play2048-app 2>/dev/null | grep "\[LLM\] qwen2.5:1.5b chosen:" | wc -l | xargs)
echo "Moves in log before benchmark: $MOVES_BEFORE"

# 1. Start a new game
curl -s -X POST "$API_BASE/game" -H "X-Client-ID: $CLIENT_ID" > /dev/null
echo "New game started."

# 2. Set AI type to LLM
curl -s -X PUT "$API_BASE/settings" -H "X-Client-ID: $CLIENT_ID" \
     -H "Content-Type: application/json" \
     -d '{"aiType": "LLM"}' > /dev/null
echo "AI type set to LLM."

START_TIME=$(date +%s)
echo "Start time: $(date -r $START_TIME)"

# 3. Trigger Auto-Play
curl -s -X POST "$API_BASE/game/auto-play" -H "X-Client-ID: $CLIENT_ID" > /dev/null
echo "Auto-play triggered."

# 4. Poll game state
while true; do
    STATE=$(curl -s -X GET "$API_BASE/game/current" -H "X-Client-ID: $CLIENT_ID")
    if [ $? -ne 0 ]; then
        echo "Error fetching game state. Retrying..."
        sleep 2
        continue
    fi
    GAME_OVER=$(echo "$STATE" | jq -r '.gameOver')
    WON=$(echo "$STATE" | jq -r '.won')
    SCORE=$(echo "$STATE" | jq -r '.score')
    
    echo "Current Score: $SCORE, Game Over: $GAME_OVER, Won: $WON"
    
    if [ "$GAME_OVER" == "true" ] || [ "$WON" == "true" ]; then
        break
    fi
    sleep 3
done

END_TIME=$(date +%s)
echo "End time: $(date -r $END_TIME)"

DURATION=$((END_TIME - START_TIME))

# 5. Count moves from logs
MOVES_AFTER=$(docker compose logs play2048-app 2>/dev/null | grep "\[LLM\] qwen2.5:1.5b chosen:" | wc -l | xargs)
MOVES=$((MOVES_AFTER - MOVES_BEFORE))

echo ""
echo "--- Final Benchmark Results ---"
echo "Final Outcome: $(if [ "$WON" == "true" ]; then echo "Win"; else echo "Loss"; fi)"
echo "Final Score: $SCORE"
echo "Total Game Duration: $DURATION seconds"
echo "Total Number of Moves: $MOVES"
if [ "$MOVES" -gt 0 ]; then
    LATENCY=$(echo "scale=2; $DURATION / $MOVES" | bc)
    echo "Average Latency per Move: $LATENCY seconds"
else
    echo "Average Latency per Move: N/A"
fi
