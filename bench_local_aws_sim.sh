#!/usr/bin/env bash
# simulating AWS c7a.xlarge: 4 vCPU, 8GB RAM

if [[ "${TRACE-0}" == "1" ]]; then set -o xtrace; fi


TEST_NAME="${1:-techascent_con}"
RUNS="${2:-5}"


function run_bench() {
    local posts=$1
    local label=$2

    echo "--- $label ($posts posts) ---"
    python gen_fake_posts.py "$posts"

    cd techascent
    clj -J-XX:ActiveProcessorCount=4 -J-Xmx8g -M -m related.core >/dev/null 2>&1
    cd ..
    python -c "
import json
with open('related_posts_techascent.json') as f:
    posts = json.load(f)
correct = {p['_id']: sum(len(set(r['tags']) & set(p['tags'])) for r in p['related']) for p in posts}
json.dump(correct, open('correct_related.json', 'w'))
"

    case "$TEST_NAME" in
        techascent)
            cd techascent
            ;;
        techascent_con)
            cd techascent_con
            ;;
        *)
            echo "Unknown test: $TEST_NAME"
            exit 1
            ;;
    esac

    echo "Results:"
    for i in $(seq 1 $RUNS); do
        clj -J-XX:ActiveProcessorCount=4 -J-Xmx8g -M -m related.core
    done

    cd ..

    case "$TEST_NAME" in
        techascent)
            python verify.py related_posts_techascent.json
            ;;
        techascent_con)
            python verify.py related_posts_techascent_con.json
            ;;
    esac
    echo ""
}


function main() {
    cd "$(dirname "$0")"

    echo "=============================================="
    echo "AWS c7a.xlarge Simulation (4 vCPU, 8GB RAM)"
    echo "Test: $TEST_NAME"
    echo "=============================================="
    echo ""

    run_bench 5000 "5k posts"
    run_bench 20000 "20k posts"
    run_bench 60000 "60k posts"

    echo "=============================================="
    echo "Done!"
    echo "=============================================="
}


if [ "$0" = "${BASH_SOURCE[@]}" ]; then
    set -o errexit
    set -o nounset
    set -o pipefail

    main
fi
