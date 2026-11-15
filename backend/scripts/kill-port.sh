#!/usr/bin/env bash
# 지정한 포트를 점유(LISTEN)하고 있는 프로세스를 찾아 종료한다.
# 로컬 개발 시 앱을 실행하기 전에 이전에 띄운 서버를 정리하는 용도.
#
# 사용법:
#   scripts/kill-port.sh          # 기본 포트 8080
#   scripts/kill-port.sh 8000     # 다른 포트 지정

set -euo pipefail

PORT="${1:-8080}"

if ! [[ "$PORT" =~ ^[0-9]+$ ]]; then
    echo "포트는 숫자여야 합니다: $PORT" >&2
    exit 1
fi

# 해당 포트에서 LISTEN 중인 프로세스의 PID 목록 (없으면 빈 문자열)
PIDS=$(lsof -t -iTCP:"$PORT" -sTCP:LISTEN || true)

if [[ -z "$PIDS" ]]; then
    echo "포트 $PORT 를 사용 중인 프로세스가 없습니다."
    exit 0
fi

for PID in $PIDS; do
    echo "포트 $PORT 사용 중: PID $PID ($(ps -o comm= -p "$PID" 2>/dev/null || echo '?'))"
done

# 먼저 정상 종료(SIGTERM)를 요청한다. Spring Boot는 이 신호를 받으면 정리 작업 후 종료한다.
kill $PIDS 2>/dev/null || true

# 최대 10초 동안 종료되기를 기다린다.
for _ in $(seq 1 10); do
    if ! lsof -t -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
        echo "종료했습니다. 포트 $PORT 가 비었습니다."
        exit 0
    fi
    sleep 1
done

# 그래도 남아 있으면 강제 종료(SIGKILL)한다.
echo "정상 종료되지 않아 강제 종료합니다."
kill -9 $(lsof -t -iTCP:"$PORT" -sTCP:LISTEN) 2>/dev/null || true
sleep 1

if lsof -t -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "포트 $PORT 를 비우지 못했습니다." >&2
    exit 1
fi
echo "강제 종료했습니다. 포트 $PORT 가 비었습니다."
