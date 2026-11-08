/* ============================================================
   Barcode Scanner Module (Standalone)
   Depends on: html5-qrcode (diload dynamic)
   Exposes: window.BarcodeScanner
   ============================================================ */

(function() {
    'use strict';

    // ============================================================
    // GUARD: cegah double-init
    // ============================================================
    if (window.BarcodeScanner && window.BarcodeScanner.__initialized) {
        console.log('[BarcodeScanner] Already initialized');
        return;
    }

    console.log('[BarcodeScanner] Initializing...');

    // ============================================================
    // STATE
    // ============================================================
    let scanner = null;
    let callback = null;
    let modalEl = null;
    let isProcessing = false;
    let libraryReady = false;
    let libraryPromise = null;
    let isOpening = false;

    // ============================================================
    // LIBRARY LOADER
    // ============================================================
    function loadLibrary() {
        if (libraryPromise) return libraryPromise;

        libraryPromise = new Promise(function(resolve, reject) {
            if (typeof window.Html5Qrcode !== 'undefined') {
                libraryReady = true;
                resolve();
                return;
            }

            const existing = document.querySelector('script[src*="html5-qrcode"]');
            if (existing) {
                if (existing.dataset.loaded === '1') {
                    libraryReady = true;
                    resolve();
                    return;
                }
                existing.addEventListener('load', function() {
                    existing.dataset.loaded = '1';
                    libraryReady = true;
                    resolve();
                });
                existing.addEventListener('error', function() {
                    reject(new Error('Failed to load library'));
                });
                return;
            }

            const script = document.createElement('script');
            script.src = 'https://unpkg.com/html5-qrcode@2.3.8/html5-qrcode.min.js';
            script.async = true;
            script.onload = function() {
                script.dataset.loaded = '1';
                libraryReady = true;
                console.log('[BarcodeScanner] ✅ Library loaded');
                resolve();
            };
            script.onerror = function() {
                reject(new Error('Failed to load html5-qrcode'));
            };
            document.head.appendChild(script);

            setTimeout(function() {
                if (!libraryReady) reject(new Error('Library load timeout'));
            }, 15000);
        });

        return libraryPromise;
    }

    // ============================================================
    // UI HELPERS
    // ============================================================
    function $id(id) { return document.getElementById(id); }

    function showLoading() {
        const el = $id('scannerLoading');
        if (el) el.style.display = 'flex';
        hideError();
        hideHint();
    }
    function hideLoading() {
        const el = $id('scannerLoading');
        if (el) el.style.display = 'none';
    }
    function showError(msg) {
        const box = $id('scannerErrorBox');
        const txt = $id('scannerErrorMsg');
        if (box && txt) { txt.textContent = msg; box.style.display = 'flex'; }
        hideLoading();
        hideHint();
    }
    function hideError() {
        const el = $id('scannerErrorBox');
        if (el) el.style.display = 'none';
    }
    function showHint() {
        const el = $id('scannerHintOverlay');
        if (el) el.style.display = 'flex';
    }
    function hideHint() {
        const el = $id('scannerHintOverlay');
        if (el) el.style.display = 'none';
    }
    function setSubtitle(text, color) {
        const el = $id('scannerSubtitle');
        if (el) { el.textContent = text; el.style.color = color || ''; }
    }
    function showStatus(text) {
        const box = $id('scannerStatus');
        const txt = $id('scannerStatusText');
        if (box && txt) { txt.textContent = text; box.style.display = 'block'; }
    }
    function hideStatus() {
        const el = $id('scannerStatus');
        if (el) el.style.display = 'none';
    }

    // ============================================================
    // MODAL INIT
    // ============================================================
    function initModal() {
        if (modalEl) return true;

        modalEl = $id('scannerModal');
        if (!modalEl) {
            console.error('[BarcodeScanner] ❌ #scannerModal not found');
            return false;
        }

        console.log('[BarcodeScanner] Modal found, binding handlers');

        const closeBtn = $id('scannerCloseBtn');
        if (closeBtn) closeBtn.addEventListener('click', close);

        const retryBtn = $id('scannerRetryBtn');
        if (retryBtn) {
            retryBtn.addEventListener('click', async function() {
                hideError();
                showLoading();
                try {
                    await requestCameraPermission();
                    await loadLibrary();
                    await startCamera();
                    hideLoading();
                    setSubtitle('Arahkan kamera ke barcode', '');
                } catch (err) {
                    handleCameraError(err);
                }
            });
        }

        const manualBtn = $id('scannerManualBtn');
        const manualInput = $id('scannerManualInput');

        if (manualBtn && manualInput) {
            manualBtn.addEventListener('click', function() {
                const val = manualInput.value.trim();
                if (val) {
                    manualInput.value = '';
                    onDetected(val, null);
                }
            });

            manualInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    manualBtn.click();
                }
            });

            manualInput.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') close();
            });
        }

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && modalEl && modalEl.style.display === 'block') {
                close();
            }
        });

        return true;
    }

    // ============================================================
    // PERMISSION
    // ============================================================
    async function requestCameraPermission() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            throw new Error('Browser tidak mendukung akses kamera');
        }

        if (location.protocol !== 'https:' &&
            location.hostname !== 'localhost' &&
            location.hostname !== '127.0.0.1') {
            throw new Error('Kamera membutuhkan HTTPS');
        }

        if (navigator.permissions && navigator.permissions.query) {
            try {
                const res = await navigator.permissions.query({ name: 'camera' });
                console.log('[BarcodeScanner] Permission:', res.state);
                if (res.state === 'denied') {
                    throw new Error('Izin kamera ditolak. Buka pengaturan browser.');
                }
            } catch (e) {
                console.log('[BarcodeScanner] Permission query not supported');
            }
        }

        try {
            const stream = await navigator.mediaDevices.getUserMedia({
                video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } },
                audio: false
            });
            console.log('[BarcodeScanner] ✅ Permission granted');
            stream.getTracks().forEach(function(t) { t.stop(); });
            await new Promise(function(r) { setTimeout(r, 300); });
        } catch (err) {
            console.warn('[BarcodeScanner] ❌ Permission error:', err.name);
            throw err;
        }
    }

    // ============================================================
    // START CAMERA
    // ============================================================
    async function startCamera() {
        if (typeof window.Html5Qrcode === 'undefined') {
            throw new Error('Library belum siap');
        }

        const readerEl = $id('scannerReader');
        if (!readerEl) return;
        readerEl.innerHTML = '';

        const formats = [
            Html5QrcodeSupportedFormats.QR_CODE,
            Html5QrcodeSupportedFormats.EAN_13,
            Html5QrcodeSupportedFormats.EAN_8,
            Html5QrcodeSupportedFormats.CODE_128,
            Html5QrcodeSupportedFormats.CODE_39,
            Html5QrcodeSupportedFormats.CODE_93,
            Html5QrcodeSupportedFormats.UPC_A,
            Html5QrcodeSupportedFormats.UPC_E,
            Html5QrcodeSupportedFormats.ITF,
            Html5QrcodeSupportedFormats.CODABAR,
            Html5QrcodeSupportedFormats.DATA_MATRIX,
            Html5QrcodeSupportedFormats.PDF_417
        ];

        const config = {
            fps: 15,
            qrbox: function(viewW, viewH) {
                const min = Math.min(viewW, viewH);
                const size = Math.floor(min * 0.7);
                return { width: size, height: size };
            },
            aspectRatio: 1.0,
            disableFlip: false
        };

        const strategies = [
            { name: 'back-camera', constraint: { facingMode: 'environment' } },
            { name: 'front-camera', constraint: { facingMode: 'user' } },
            { name: 'any-camera', constraint: { facingMode: 'any' } }
        ];

        let lastError = null;

        for (const strat of strategies) {
            try {
                console.log('[BarcodeScanner] Trying:', strat.name);

                scanner = new Html5Qrcode('scannerReader', {
                    formatsToSupport: formats,
                    verbose: false,
                    experimentalFeatures: { useBarCodeDetectorIfSupported: true }
                });

                await scanner.start(strat.constraint, config, onDetected, function() {});

                console.log('[BarcodeScanner] ✅ Started:', strat.name);
                return;

            } catch (err) {
                console.warn('[BarcodeScanner] ❌ Failed:', strat.name, err.name);
                lastError = err;

                try {
                    if (scanner) {
                        if (scanner.isScanning) await scanner.stop();
                        scanner.clear();
                    }
                } catch (e) {}
                scanner = null;

                await new Promise(function(r) { setTimeout(r, 300); });
            }
        }

        // Fallback: getCameras
        try {
            console.log('[BarcodeScanner] Trying: getCameras');
            const devices = await Html5Qrcode.getCameras();
            if (devices && devices.length > 0) {
                const back = devices.find(function(d) {
                    const l = (d.label || '').toLowerCase();
                    return l.includes('back') || l.includes('belakang') ||
                           l.includes('rear') || l.includes('environment');
                });
                const cameraId = back ? back.id : devices[0].id;

                scanner = new Html5Qrcode('scannerReader', {
                    formatsToSupport: formats,
                    verbose: false
                });

                await scanner.start(cameraId, config, onDetected, function() {});
                console.log('[BarcodeScanner] ✅ Started via getCameras');
                return;
            }
        } catch (err) {
            console.warn('[BarcodeScanner] getCameras failed:', err);
            lastError = err;
        }

        throw lastError || new Error('Kamera tidak dapat diakses');
    }

    // ============================================================
    // DETECTION
    // ============================================================
    function onDetected(decodedText, decodedResult) {
        if (isProcessing) return;
        isProcessing = true;

        if (navigator.vibrate) {
            try { navigator.vibrate(100); } catch (e) {}
        }

        showStatus(decodedText);
        stopCamera();

        setTimeout(function() {
            const cb = callback;
            close();
            if (typeof cb === 'function') {
                try {
                    cb(decodedText, decodedResult);
                } catch (e) {
                    console.error('[BarcodeScanner] Callback error:', e);
                }
            }
        }, 350);
    }

    // ============================================================
    // STOP
    // ============================================================
    async function stopCamera() {
        if (!scanner) return;
        const s = scanner;
        scanner = null;
        try {
            if (s.isScanning) await s.stop();
            s.clear();
        } catch (err) {
            console.warn('[BarcodeScanner] Stop error:', err);
        }
    }

    // ============================================================
    // ERROR HANDLER
    // ============================================================
    function handleCameraError(err) {
        hideLoading();

        let message = 'Kamera tidak tersedia — gunakan input manual di bawah';

        if (err) {
            switch (err.name) {
                case 'NotAllowedError':
                case 'PermissionDeniedError':
                    message = 'Izin kamera ditolak. Aktifkan kamera di pengaturan browser.';
                    break;
                case 'NotFoundError':
                case 'DevicesNotFoundError':
                    message = 'Kamera tidak ditemukan di perangkat ini.';
                    break;
                case 'NotReadableError':
                case 'TrackStartError':
                    message = 'Kamera sedang digunakan aplikasi lain.';
                    break;
                case 'OverconstrainedError':
                    message = 'Kamera tidak mendukung resolusi yang diminta.';
                    break;
                case 'SecurityError':
                    message = 'Akses kamera diblokir. Pastikan HTTPS.';
                    break;
                default:
                    if (err.message) message = err.message;
            }
        }

        showError(message);
        setSubtitle('Kamera tidak tersedia', '#FBBF24');

        setTimeout(function() {
            const mi = $id('scannerManualInput');
            if (mi) mi.focus();
        }, 200);
    }

    // ============================================================
    // OPEN
    // ============================================================
    async function open(options) {
        options = options || {};

        if (isOpening) return;
        if (modalEl && modalEl.style.display === 'block') return;

        isOpening = true;
        callback = options.onDetected || null;

        if (!initModal()) {
            isOpening = false;
            alert('Scanner tidak tersedia. Muat ulang halaman.');
            return;
        }

        isProcessing = false;
        hideStatus();
        hideError();
        hideHint();

        setSubtitle(options.title || 'Arahkan kamera ke barcode', '');

        modalEl.style.display = 'block';
        document.body.style.overflow = 'hidden';

        setTimeout(function() {
            const mi = $id('scannerManualInput');
            if (mi) mi.focus();
        }, 100);

        showLoading();

        try {
            await requestCameraPermission();
            await loadLibrary();
            await startCamera();
            hideLoading();
            setSubtitle('Arahkan kamera ke barcode', '');
        } catch (err) {
            console.warn('[BarcodeScanner] Open error:', err);
            handleCameraError(err);
        } finally {
            isOpening = false;
        }
    }

    // ============================================================
    // CLOSE
    // ============================================================
    function close() {
        stopCamera();
        if (modalEl) modalEl.style.display = 'none';
        document.body.style.overflow = '';

        const mi = $id('scannerManualInput');
        if (mi) mi.value = '';

        hideStatus();
        hideError();
        hideLoading();
        hideHint();

        callback = null;
        isProcessing = false;
        isOpening = false;
    }

    // ============================================================
    // EXPOSE API — PALING PENTING!
    // ============================================================
    window.BarcodeScanner = {
        open: open,
        close: close,
        isReady: function() { return !!modalEl; },
        isOpen: function() { return !!modalEl && modalEl.style.display === 'block'; },
        __initialized: true
    };

    console.log('[BarcodeScanner] ✅ API exposed to window.BarcodeScanner');

    // ============================================================
    // AUTO INIT MODAL (retry jika belum ada di DOM)
    // ============================================================
    function autoInit() {
        if (initModal()) {
            console.log('[BarcodeScanner] ✅ Modal initialized');
        } else {
            let tries = 0;
            const retry = setInterval(function() {
                tries++;
                if (initModal()) {
                    clearInterval(retry);
                    console.log('[BarcodeScanner] ✅ Modal initialized (delayed)');
                } else if (tries > 20) {
                    clearInterval(retry);
                    console.warn('[BarcodeScanner] ⚠ Modal not found after 10s');
                }
            }, 500);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', autoInit);
    } else {
        autoInit();
    }

    // ============================================================
    // WEDGE SCANNER (USB/Bluetooth keyboard)
    // ============================================================
    let wedgeBuffer = '';
    let wedgeTimer = null;

    document.addEventListener('keydown', function(e) {
        const tag = (e.target && e.target.tagName) || '';
        if (['INPUT', 'TEXTAREA', 'SELECT'].includes(tag)) return;
        if (modalEl && modalEl.style.display === 'block') return;
        if (e.key.length !== 1) return;

        wedgeBuffer += e.key;

        clearTimeout(wedgeTimer);
        wedgeTimer = setTimeout(function() {
            if (wedgeBuffer.length >= 6) {
                const barcode = wedgeBuffer;
                wedgeBuffer = '';
                document.dispatchEvent(new CustomEvent('barcode:scanned', {
                    detail: { barcode: barcode }
                }));
            } else {
                wedgeBuffer = '';
            }
        }, 100);
    });

})();