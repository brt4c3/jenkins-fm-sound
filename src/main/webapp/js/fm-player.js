function playFMFromJSON(jsonStr) {
  const data = JSON.parse(jsonStr).data;
  const context = new (window.AudioContext || window.webkitAudioContext)();
  let frameIndex = 0;

  function playFrame() {
    if (frameIndex >= data.length) return;

    const char = data[frameIndex].charCodeAt(0);
    const gain = char / 127.0;
    const duration = 1 / 24;

    const carrier = context.createOscillator();
    carrier.frequency.value = 440;

    const modulator = context.createOscillator();
    modulator.frequency.value = 3;

    const modulationGain = context.createGain();
    modulationGain.gain.value = gain * 100;

    modulator.connect(modulationGain);
    modulationGain.connect(carrier.frequency);
    carrier.connect(context.destination);

    carrier.start();
    modulator.start();
    carrier.stop(context.currentTime + duration);
    modulator.stop(context.currentTime + duration);

    frameIndex++;
    setTimeout(playFrame, duration * 1000);
  }

  playFrame();
}
