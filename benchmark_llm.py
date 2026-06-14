import requests
import time
import sys
import json
from datetime import datetime
import subprocess

BASE_URL = "http://localhost:8085"
CLIENT_ID = "exhaustive-llm-pro"
HEADERS = {"X-Client-ID": CLIENT_ID}

def start_game():
    print(f"Starting new game for client: {CLIENT_ID}")
    resp = requests.post(f"{BASE_URL}/api/game", headers=HEADERS)
    resp.raise_for_status()
    return resp.json()

def set_ai_strategy():
    print("Setting AI strategy to LLM")
    resp = requests.put(f"{BASE_URL}/api/settings", headers=HEADERS, json={"aiType": "LLM"})
    resp.raise_for_status()

def trigger_auto_play():
    print("Triggering Auto-Play")
    resp = requests.post(f"{BASE_URL}/api/game/auto-play", headers=HEADERS)
    resp.raise_for_status()

def get_current_game():
    resp = requests.get(f"{BASE_URL}/api/game/current", headers=HEADERS)
    resp.raise_for_status()
    return resp.json()

def run_benchmark():
    start_time = datetime.now()
    print(f"Benchmark started at: {start_time}")
    
    start_game()
    set_ai_strategy()
    trigger_auto_play()
    
    highest_tile = 0
    final_score = 0
    
    while True:
        try:
            game_state = get_current_game()
            final_score = game_state['score']
            board = game_state['boardState']['tiles']
            current_max = max(max(row) for row in board)
            if current_max > highest_tile:
                highest_tile = current_max
            
            print(f"[{datetime.now().strftime('%H:%M:%S')}] Score: {final_score}, Max Tile: {highest_tile}")
            
            if game_state['gameOver'] or game_state['won']:
                print(f"Game finished! Won: {game_state['won']}, GameOver: {game_state['gameOver']}")
                break
                
        except Exception as e:
            print(f"Error polling game state: {e}")
            
        time.sleep(10)
        
        # Status report if more than 10 mins
        elapsed = (datetime.now() - start_time).total_seconds()
        if elapsed > 600 and int(elapsed) % 600 < 10:
             print(f"STATUS REPORT: 10+ minutes elapsed. Current Score: {final_score}, Max Tile: {highest_tile}")

    end_time = datetime.now()
    duration = (end_time - start_time).total_seconds()
    
    # Analyze logs for move count and latency
    total_moves = 0
    try:
        logs = subprocess.check_output(["docker", "compose", "logs", "play2048-app"], text=True)
        total_moves = logs.count("[LLM] Hardened choice:")
    except (subprocess.CalledProcessError, FileNotFoundError) as e:
        print(f"Could not get logs: {e}")

    avg_latency = duration / total_moves if total_moves > 0 else 0

    print("\n--- BENCHMARK RESULTS ---")
    print(f"Final Score: {final_score}")
    print(f"Total Duration: {duration:.2f} seconds")
    print(f"Highest Tile Reached: {highest_tile}")
    print(f"Total Moves: {total_moves}")
    print(f"Average Latency per Move: {avg_latency:.2f} seconds")
    print(f"End Time: {end_time}")
    
    return final_score, duration, highest_tile

if __name__ == "__main__":
    run_benchmark()
