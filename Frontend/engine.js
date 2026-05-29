// ==========================================
// CONFIGURATION & STATE
// ==========================================
//const API_BASE = 'http://localhost:8080/api/v1';
const API_BASE_URL = "https://my-math-engine-backend.onrender.com/api/v1";
let mathChartInstance = null;
const MAX_HISTORY_ITEMS = 50;

// ==========================================
// 🚀 OPTIMIZATION: THE CENTRAL API WRAPPER
// ==========================================
// Every network request routes through this single function.
// It automatically attaches your API key and handles the JSON.

/*
async function apiPost(endpoint, payload) {
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-API-KEY': config.API_KEY // The VIP pass is applied automatically!
        },
        body: JSON.stringify(payload)
    });
    return response; // Return the raw response so callers can handle specific errors
}*/

async function apiPost(endpoint, payload) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, { // <-- Updated variable name
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-API-KEY': config.API_KEY 
        },
        body: JSON.stringify(payload)
    });
    return response; 
}

// ==========================================
// UI / STATE FUNCTIONS
// ==========================================
function toggleHistory() {
    const sidebar = document.getElementById('historySidebar');
    sidebar.classList.toggle('active');
    
    if (sidebar.classList.contains('active')) {
        renderHistory();
    }
}

// ==========================================
// 1. MIDDLEWARE TRANSLATOR
// ==========================================
function translateLaTeXToEngine(latex) {
    let engineStr = latex;

    // A. Pre-processing (Chained for slight speed boost)
    engineStr = engineStr.replace(/\\[;,:\s!]/g, '')
                         .replace(/\\left\(/g, '(').replace(/\\right\)/g, ')')
                         .replace(/\\displaystyle/g, '')
                         .replace(/\\textstyle/g, '')
                         .replace(/\\limits/g, '')
                         .replace(/−/g, '-'); 

    // Upgraded 'dx' normalizer 
    engineStr = engineStr.replace(/\\(mathrm|operatorname|text|differentialD|mathit)?\s*\{?d\}?\s*([a-zA-Z])/g, 'd$2');

    // B. AUTO-REARRANGE EQUATIONS
    if (engineStr.includes('=')) {
        const parts = engineStr.split('=');
        if (parts.length === 2) {
            engineStr = `(${parts[0]}) - (${parts[1]})`;
        }
    }

    // C. Translate Integrals & Math
    const intRegex = /\\int\s*_\s*(\{([^}]+)\}|([a-zA-Z0-9.\-]+))\s*\^\s*(\{([^}]+)\}|([a-zA-Z0-9.\-]+))\s*(.*?)\s*d([a-zA-Z])/g;
    engineStr = engineStr.replace(intRegex, (match, p1, braceLower, singleLower, p4, braceUpper, singleUpper, expr, varName) => {
        const lower = braceLower || singleLower;
        const upper = braceUpper || singleUpper;
        return `integral(${expr}, ${varName}, ${lower}, ${upper})`;
    });

    const indefRegex = /\\int\s*(.*?)\s*d([a-zA-Z])/g;
    engineStr = engineStr.replace(indefRegex, 'indefinite($1, $2)');

    const fracRegex = /\\frac\{([^}]+)\}\{([^}]+)\}/g;
    while(fracRegex.test(engineStr)) engineStr = engineStr.replace(fracRegex, '($1)/($2)');
    
    engineStr = engineStr.replace(/\\sqrt\{([^}]+)\}/g, 'sqrt($1)')
                         
    engineStr = engineStr.replace(/\\(sin|cos|tan|log|ln)/g, '$1');
    engineStr = engineStr.replace(/(sin|cos|tan|log|ln)\s*\(*\s*([a-zA-Z0-9.]+)\s*\)*/g, '$1($2)');

    // D. Virtual Keyboard Operators
    engineStr = engineStr.replace(/\\times/g, '*')
                         .replace(/\\cdot/g, '*')
                         .replace(/\\div/g, '/')
                         .replace(/\\ast/g, '*')
                         .replace(/\\pi/g, '3.14159265359')
                         .replace(/\\exponentialE/g, '2.71828182846')
                         .replace(/\\infty/g, 'Infinity'); 

    // E. The Nuclear Option (Strip unrecognized tags)
    engineStr = engineStr.replace(/\\[a-zA-Z]+/g, '').replace(/\\/g, '');

    return engineStr.trim();
}

// ==========================================
// 2. THE SMART UNIFIED ROUTER
// ==========================================
async function executeEngine() {
    const mf = document.getElementById('equation');
    const rawLaTeX = mf.getValue('latex');
    const errorBox = document.getElementById('errorBox');
    errorBox.innerText = '';

    if (!rawLaTeX) return;

    const translatedEquation = translateLaTeXToEngine(rawLaTeX);
    console.log("Translated String:", translatedEquation);

    const isEquation = rawLaTeX.includes('=');
    const hasX = rawLaTeX.includes('x');
    const isCubicOrHigher = rawLaTeX.includes('^3') || rawLaTeX.includes('^4');
    const isCalculus = translatedEquation.includes('integral(') || translatedEquation.includes('indefinite(');

    if (isEquation && hasX && !isCubicOrHigher) {
        console.log("🚀 ROUTING: Detected Polynomial Equation. Engaging 3-Point Extractor...");
        await solveSmartQuadratic(translatedEquation);
    } else if (isCalculus) {
        console.log("🧠 ROUTING: Detected Calculus. Engaging CAS Brain...");
        await solveCalculusMath(translatedEquation);
    } else {
        console.log("⚙️ ROUTING: Detected Standard Expression/Calculus.");
        let variables = {};
        const varInput = document.getElementById('variables') ? document.getElementById('variables').value.trim() : "";
        if (varInput) variables = JSON.parse(varInput);

        await calculateMath(translatedEquation, variables);
        await generateGraph(translatedEquation);
    }
}

// ==========================================
// 3. THE 3-POINT QUADRATIC EXTRACTOR
// ==========================================
async function solveSmartQuadratic(equationStr) {
    const errorBox = document.getElementById('errorBox');
    
    // Uses the new wrapper
    async function evaluateAt(xValue) {
        const res = await apiPost('/calculate', { equation: equationStr, variables: { "x": xValue } });
        const data = await res.json();
        return parseFloat(data.result);
    }

    try {
        const [f0, f1, fMinus1] = await Promise.all([evaluateAt(0), evaluateAt(1), evaluateAt(-1)]);

        const c = f0;
        const a = (f1 + fMinus1 - (2 * c)) / 2;
        const b = (f1 - fMinus1) / 2;

        // Uses the new wrapper
        const response = await apiPost('/quadratic', { a: a, b: b, c: c });
        const data = await response.json();

        if (response.ok) {
            document.getElementById('metricTitle').innerText = "DISCRIMINANT (Δ)";
            document.getElementById('metrics').innerText = data.discriminant;
            
            const mathDisplay = document.getElementById('renderedMath');
            mathDisplay.innerText = '$$' + data.latexSteps + '$$';
            MathJax.typesetPromise([mathDisplay]);
            
            const mf = document.getElementById('equation');
            saveToHistory(mf.getValue('latex'), `\\Delta = ${data.discriminant}`);

            await generateGraph(equationStr);
        } else {
            errorBox.innerText = `Server Fault: ${data.error}`;
        }
    } catch (error) {
        errorBox.innerText = "Execution Fault: Failed to extract polynomial coefficients.";
    }
}

// ==========================================
// 4. NUMERICAL API CALLS & GRAPHING
// ==========================================
async function calculateMath(equation, variables) {
    const errorBox = document.getElementById('errorBox');
    try {
        // Uses the new wrapper
        const response = await apiPost('/calculate', { equation: equation, variables: variables });
        const data = await response.json();
        
        if (response.ok) {
            document.getElementById('metricTitle').innerText = "COMPUTE LATENCY";
            document.getElementById('metrics').innerText = `${data.computeTimeMs} ms`;
            
            const mathDisplay = document.getElementById('renderedMath');
            mathDisplay.innerText = '$$' + data.latexEquation + ' = ' + data.result + '$$';
            MathJax.typesetPromise([mathDisplay]);

            const mf = document.getElementById('equation');
            saveToHistory(mf.getValue('latex'), data.result);
        } else {
            errorBox.innerText = `Server Fault: ${data.error}`;
        }
    } catch (error) {
        errorBox.innerText = "Network Fault: Could not connect backend.";
    }
}

async function generateGraph(equation) {
    try {
        // Uses the new wrapper
        const response = await apiPost('/graph', { equation: equation, variable: "x", min: -10.0, max: 10.0, points: 100 });
        if (response.ok) {
            const data = await response.json();
            if (mathChartInstance) mathChartInstance.destroy();
            const ctx = document.getElementById('mathChart').getContext('2d');
            
            let gradientFill = ctx.createLinearGradient(0, 0, 0, 400);
            gradientFill.addColorStop(0, 'rgba(255, 255, 255, 0.15)'); 
            gradientFill.addColorStop(1, 'rgba(255, 255, 255, 0.0)');

            mathChartInstance = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: `f(x)`, 
                        data: data.data, 
                        borderColor: 'rgba(255, 255, 255, 0.8)', 
                        borderWidth: 2, 
                        backgroundColor: gradientFill, 
                        pointRadius: 0, 
                        fill: true, 
                        tension: 0.4
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        x: { grid: { color: 'rgba(255,255,255,0.03)' }, ticks: { color: '#94a3b8' } },
                        y: { grid: { color: 'rgba(255,255,255,0.03)' }, ticks: { color: '#94a3b8' } }
                    },
                    plugins: { legend: { labels: { color: '#f8fafc', font: { family: 'JetBrains Mono', size: 14 } } } }
                }
            });
        }
    } catch (error) { console.error(error); }
}

// ==========================================
// 5. UNIFIED SYMBOLIC CAS ENGINE
// ==========================================
async function solveCalculusMath(translatedEquation) {
    const errorBox = document.getElementById('errorBox');
    let payload = {};
    let displayLatex = "";

    const defMatch = translatedEquation.match(/integral\((.*?),\s*([a-zA-Z]),\s*(.*?),\s*(.*?)\)/);
    const indefMatch = translatedEquation.match(/indefinite\((.*?),\s*([a-zA-Z])\)/);

    if (defMatch) {
        payload = { equation: defMatch[1], variable: defMatch[2], lowerBound: defMatch[3], upperBound: defMatch[4] };
        displayLatex = `\\int_{${defMatch[3]}}^{${defMatch[4]}} ${defMatch[1]} \\, d${defMatch[2]}`;
    } else if (indefMatch) {
        payload = { equation: indefMatch[1], variable: indefMatch[2] };
        displayLatex = `\\int ${indefMatch[1]} \\, d${indefMatch[2]}`;
    } else {
        errorBox.innerText = "Execution Fault: Could not parse calculus expression.";
        return;
    }

    payload.equation = payload.equation.replace(/2\.71828182846/g, 'E')
                                       .replace(/3\.14159265359/g, 'Pi')
                                       .replace(/(?:E|e)\^\((.*?)\)/g, 'Exp($1)')
                                       .replace(/(?:E|e)\^([a-zA-Z0-9.\-]+)/g, 'Exp($1)');

    try {
        // Uses the new wrapper
        const response = await apiPost('/symbolic', payload);
        const data = await response.json();
        
        if (response.ok) {
            document.getElementById('metricTitle').innerText = "CAS COMPUTE LATENCY";
            document.getElementById('metrics').innerText = `${data.computeTimeMs} ms`;
            
            const mathDisplay = document.getElementById('renderedMath');
            mathDisplay.innerText = '$$' + displayLatex + ' = ' + data.latexEquation + '$$';
            MathJax.typesetPromise([mathDisplay]);

            const rawLaTeX = document.getElementById('equation').getValue('latex');
            saveToHistory(rawLaTeX, data.latexEquation);

            if (indefMatch && !data.result.includes("Infinity")) {
                await generateGraph(data.result);
            } else {
                await generateGraph(payload.equation);
            }
        } else {
            errorBox.innerText = `CAS Fault: ${data.error}`;
        }
    } catch (error) {
        errorBox.innerText = "Network Fault: Could not connect to CAS Engine.";
    }
}

// ==========================================
// 6. CLIENT-SIDE LOCALSTORAGE HISTORY ENGINE
// ==========================================
function saveToHistory(rawLatex, displayResult) {
    try {
        let history = JSON.parse(localStorage.getItem('mathEngineHistory')) || [];
        const newRecord = {
            latex: rawLatex,
            result: displayResult,
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        };
        
        if (history.length > 0 && history[0].latex === rawLatex) return;
        history.unshift(newRecord);
        if (history.length > MAX_HISTORY_ITEMS) history = history.slice(0, MAX_HISTORY_ITEMS);
        localStorage.setItem('mathEngineHistory', JSON.stringify(history));
    } catch (e) {
        console.error("Storage Fault: Failed to write to localStorage", e);
    }
}

function renderHistory() {
    const historyList = document.getElementById('historyList');
    const history = JSON.parse(localStorage.getItem('mathEngineHistory')) || [];
    
    if (history.length === 0) {
        historyList.innerHTML = `<div style="color: #86868b; font-size: 0.85rem; text-align: center; margin-top: 2rem;">No calculations yet.</div>`;
        return;
    }
    
    historyList.innerHTML = history.map((item, index) => `
        <div class="history-card" onclick="loadHistoryItem(${index})">
            <span class="history-time">${item.time}</span>
            <div class="history-math">
                $$${item.latex} = ${item.result}$$
            </div>
        </div>
    `).join('');
    
    if (window.MathJax && MathJax.typesetPromise) {
        MathJax.typesetPromise([historyList]).catch((err) => console.error(err));
    }
}

async function loadHistoryItem(index) {
    const history = JSON.parse(localStorage.getItem('mathEngineHistory')) || [];
    const selectedItem = history[index];
    if (!selectedItem) return;
    const mf = document.getElementById('equation');
    if (mf) {
        mf.setValue(selectedItem.latex, { insertTo: 'replaceAll' });
        toggleHistory();
        await executeEngine();
    }
}
