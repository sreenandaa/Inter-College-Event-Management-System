/**
 * InterCollege - Subtle Animated Constellation Mesh Canvas
 * Creates a slow, ambient network of connecting dots and soft glowing nodes
 * representing intercollegiate connection and event discovery.
 *
 * Designed to be ultra-lightweight, smooth, and unobtrusive to ensure 100% readability.
 */
(function () {
  const canvas = document.getElementById('bg-canvas');
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  let width, height;
  let particles = [];
  let glowNodes = [];

  // Theme Colors
  const COLOR_DOT = 'rgba(64, 138, 113, 0.75)';    // #408A71
  const COLOR_MINT = 'rgba(176, 228, 204, 0.85)';  // #B0E4CC
  const MAX_CONNECT_DISTANCE = 135;

  function resize() {
    width = canvas.width = window.innerWidth;
    height = canvas.height = window.innerHeight;
    initParticles();
  }

  function initParticles() {
    particles = [];
    glowNodes = [];

    // Density based on screen area (fewer particles on mobile for high FPS)
    const count = Math.min(Math.floor((width * height) / 18000), 75);

    for (let i = 0; i < count; i++) {
      particles.push({
        x: Math.random() * width,
        y: Math.random() * height,
        vx: (Math.random() - 0.5) * 0.45,
        vy: (Math.random() - 0.5) * 0.45,
        radius: Math.random() * 1.8 + 1.2,
        isMint: Math.random() > 0.65
      });
    }

    // 4-6 large, very faint ambient floating glow orbs
    const orbCount = Math.max(3, Math.floor(width / 350));
    for (let i = 0; i < orbCount; i++) {
      glowNodes.push({
        x: Math.random() * width,
        y: Math.random() * height,
        vx: (Math.random() - 0.5) * 0.2,
        vy: (Math.random() - 0.5) * 0.2,
        radius: Math.random() * 90 + 70,
        pulse: Math.random() * Math.PI,
        pulseSpeed: 0.01 + Math.random() * 0.015
      });
    }
  }

  function render() {
    ctx.clearRect(0, 0, width, height);

    // 1. Render soft ambient glow orbs
    for (let i = 0; i < glowNodes.length; i++) {
      const g = glowNodes[i];
      g.x += g.vx;
      g.y += g.vy;
      g.pulse += g.pulseSpeed;

      // Wrap around edges
      if (g.x < -100) g.x = width + 100;
      if (g.x > width + 100) g.x = -100;
      if (g.y < -100) g.y = height + 100;
      if (g.y > height + 100) g.y = -100;

      const currentRadius = g.radius + Math.sin(g.pulse) * 18;
      const gradient = ctx.createRadialGradient(g.x, g.y, 0, g.x, g.y, currentRadius);
      gradient.addColorStop(0, 'rgba(40, 90, 72, 0.12)');
      gradient.addColorStop(0.5, 'rgba(64, 138, 113, 0.05)');
      gradient.addColorStop(1, 'rgba(9, 20, 19, 0)');

      ctx.fillStyle = gradient;
      ctx.beginPath();
      ctx.arc(g.x, g.y, currentRadius, 0, Math.PI * 2);
      ctx.fill();
    }

    // 2. Render connecting lines between particles
    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const p1 = particles[i];
        const p2 = particles[j];
        const dx = p1.x - p2.x;
        const dy = p1.y - p2.y;
        const dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < MAX_CONNECT_DISTANCE) {
          const alpha = (1 - dist / MAX_CONNECT_DISTANCE) * 0.22;
          ctx.strokeStyle = `rgba(176, 228, 204, ${alpha})`;
          ctx.lineWidth = 0.75;
          ctx.beginPath();
          ctx.moveTo(p1.x, p1.y);
          ctx.lineTo(p2.x, p2.y);
          ctx.stroke();
        }
      }
    }

    // 3. Render individual particles
    for (let i = 0; i < particles.length; i++) {
      const p = particles[i];
      p.x += p.vx;
      p.y += p.vy;

      // Bounce/wrap at edges
      if (p.x < 0) p.x = width;
      if (p.x > width) p.x = 0;
      if (p.y < 0) p.y = height;
      if (p.y > height) p.y = 0;

      ctx.fillStyle = p.isMint ? COLOR_MINT : COLOR_DOT;
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      ctx.fill();
    }

    requestAnimationFrame(render);
  }

  // Check user preference for reduced motion
  const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (!prefersReduced) {
    window.addEventListener('resize', resize);
    resize();
    render();
  }
})();
