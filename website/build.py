"""Build the crawlable development website. SITE_URL is required for production.

python website/build.py --output website/dist
SITE_URL must be the verified deployed HTTPS origin, optionally with a path prefix.
APPSTORE_URL is optional and must be an actual Amazon product URL when configured.
"""
from __future__ import annotations

import argparse
import html
import json
import os
import re
from pathlib import Path
from urllib.parse import urlparse
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parent
PAGES = {
    "index": ("A little attention. A growing collection.", "FloraTend is a plant care and watering tracker in development for Amazon Fire tablets and compatible Android devices.", [
        ("Your own growing space", "Bring plant names, care checks and growth notes into one calm place. FloraTend is being built around local records and everyday routines, with no account required for core care."),
        ("Check the plant, then decide", "A watering reminder is an invitation to inspect the soil and plant. An elapsed interval alone does not show that a plant needs water."),
        ("Follow the project", "The app is in development and is not yet available to download. Device, billing and worldwide language release checks are still in progress.")]),
    "features": ("Make room for every plant.", "Explore the plant collection, care schedule and journal workflows planned for FloraTend.", [
        ("A personal plant collection", "Keep nicknames, species, locations and personal notes together. Search your collection and return to the details that matter to you."),
        ("A care routine you control", "Plan soil checks and other care tasks, record what you did, and adapt your routine to real conditions."),
        ("A record of change", "Use dated notes and your own photographs to record new leaves, flowering and observations. Measurements come from your records; task completion does not establish plant health.")]),
    "plant-watering-reminders": ("A reminder to check in.", "Understand how condition-led watering reminders can support a manageable plant care routine.", [
        ("How should I organise watering schedules?", "Use a schedule to remember to inspect each plant. Check moisture and drainage, consider growing conditions and species-specific guidance, and decide whether watering is appropriate."),
        ("Room for changing conditions", "Temperature, light, season, potting mix and container size can affect watering needs. Review intervals as conditions change."),
        ("The FloraTend approach", "The planned care workflow records completed actions and offers check dates, snoozing and paused schedules. Dates are reminders, not guaranteed watering instructions.")]),
    "houseplant-tracker": ("Every plant, in its place.", "Learn how a houseplant tracker can organise names, locations, photographs and care notes.", [
        ("How can I track my houseplants?", "Start with a nickname, known species and location. Add pot and light notes that help you understand the conditions around that plant."),
        ("Personal notes stay personal", "Your plant names and journal entries should remain exactly as you entered them. Interface localisation is separate from your own writing."),
        ("A collection that stays useful", "FloraTend is designed to keep saved plant records available locally, without a required account. Offline and restart workflows are subject to release testing.")]),
    "plant-care-calendar": ("A clearer view of the week.", "Plan plant care checks, fertilising, pruning and repotting with a personal care calendar.", [
        ("What belongs in a care calendar?", "Use it for soil checks, observations and tasks you choose. Refer to reliable species-specific guidance and product labels when considering fertilisers or treatments."),
        ("Record what happened", "A useful calendar connects planned tasks with completion history. It helps you see your routine without implying that every completed task improves plant health."),
        ("Built for real routines", "FloraTend's calendar is being developed alongside recurrence and timezone handling. Final supported behaviour will follow verified app release checks.")]),
    "growth-journal": ("Notice the new leaf.", "Keep plant growth notes and photographs using your own dated observations.", [
        ("Can I track plant growth with photos?", "Consistent photographs and dated notes make changes easier to compare. Use similar framing and record actual measurements rather than estimating growth from a picture."),
        ("More than a measurement", "Flowering, leaf counts and general observations can be meaningful parts of a journal. Symptoms alone are not a diagnosis; persistent or severe problems may need expert advice."),
        ("Your record, over time", "FloraTend is being built with a local growth journal. Photo timelines and advanced comparisons are planned Premium features, subject to release verification.")]),
    "voice-features": ("Speak, review, then care.", "Learn about FloraTend's push-to-talk voice approach and device capability limits.", [
        ("Can I manage plants using voice commands?", "FloraTend is being developed with push-to-talk commands and spoken responses where compatible Android speech recognition and TextToSpeech engines are available."),
        ("Availability depends on your device", "Fire tablets may not include a compatible recognition engine. Voice language support depends on the installed engine and differs from interface language support. Offline recognition is not promised."),
        ("Touch remains available", "Essential care actions are designed for touch and text too. Microphone access is requested when used; there is no planned continuous background listening or default raw-audio storage.")]),
    "fire-tablet-compatibility": ("More space for a growing collection.", "Follow FloraTend development for Amazon Fire tablets and compatible Android devices.", [
        ("Can I manage plant care on a Fire tablet?", "FloraTend is being built as a native Android app with Fire tablets as its primary platform. The final supported device list will be based on actual release testing."),
        ("Designed for a tablet", "Portrait, landscape and larger-screen layouts are part of the development brief. Seven-, eight- and ten-inch configurations require verification before compatibility is claimed."),
        ("No universal voice promise", "Speech engines, camera capabilities and operating-system versions vary. A device-specific compatibility matrix will distinguish tested features from unverified ones.")]),
    "pricing": ("Start with a few plants.", "Read the proposed FloraTend free and Premium model while store configuration is in development.", [
        ("A proposed free tier", "The planned free tier includes up to five plants, manual plant entry, basic care reminders and basic growth notes."),
        ("A proposed one-time unlock", "FloraTend Premium is planned to add unlimited plants and advanced care, journal, insights and export features. Products and regional pricing are not final."),
        ("Store pricing at purchase", "An available release will show the price supplied by Amazon for your marketplace. There is no checkout on this website, and no purchase is currently offered here.")]),
    "faq": ("A little more clarity.", "Answers about FloraTend's development, offline care records, voice availability and plant guidance.", [
        ("Is FloraTend available now?", "No. The app is in development. Release requires verified functionality, supported devices, billing, translations and store assets."),
        ("Are there offline plant care apps?", "FloraTend is designed to keep core plant records, care schedules and journal notes local. Its complete offline workflows must pass release testing before availability is announced."),
        ("Will it identify a plant from a photo?", "Photo identification is optional and is not currently advertised as an available feature. A configured, tested engine and explicit consent for any external image transmission would be required."),
        ("Are watering reminders a diagnosis?", "No. Reminder dates and observations are organisational and educational tools. They do not establish moisture needs or diagnose a plant problem.")]),
    "privacy": ("Your collection. Your control.", "Read the FloraTend development privacy notice and planned local-first defaults.", [
        ("Development privacy notice", "This notice describes the current development policy and is not a final release privacy policy. The publisher's legal identity, contact details and applicable rights process must be completed before launch."),
        ("Local records by default", "The app is designed to store plant records, notes, settings and selected photographs locally. Core plant care does not require an account. No third-party advertising SDKs are planned for version one."),
        ("Optional capabilities", "Speech recognition and TextToSpeech depend on installed device engines, whose processing may involve their providers. Photo identification and cloud services must remain unavailable until configured, disclosed and consented to as appropriate."),
        ("Export and deletion", "Release privacy review must verify export destinations, deletion behaviour, backup scope and actual network traffic. A local export leaves app storage at the destination the user chooses."),
        ("This website", "These static pages include no analytics scripts, tracking pixels, forms or third-party media embeds. A deployed hosting provider may process ordinary request data; its details must be added before publication.")]),
    "support": ("A little help along the way.", "Find development support information and practical troubleshooting for FloraTend.", [
        ("Before the first release", "There is no public app download or live support service yet. The publisher must provide a monitored support channel before release."),
        ("Voice is unavailable", "A missing speech recognition or TextToSpeech engine can limit voice features. Use touch and text controls, and check your device's installed language and speech settings."),
        ("Care guidance", "Plant symptoms can have several causes. Record observations and seek qualified local horticultural advice for persistent or severe problems. Do not infer treatment dosages from a reminder."),
        ("Release support checklist", "Support contact details, a published privacy policy and a verified Amazon listing link will be added when they exist. No contact address or store listing is invented here.")]),
}

CSS = """@font-face{font-family:FloraFallback;src:local('Georgia')}*{box-sizing:border-box}body{margin:0;background:#faf8f2;color:#2d332e;font-family:system-ui,sans-serif;line-height:1.7}a{color:#315c45;text-underline-offset:4px}a:focus-visible{outline:3px solid #c8795b;outline-offset:6px}header{max-width:1120px;margin:auto;padding:28px 32px;border-bottom:1px solid #d9dfd4;display:flex;justify-content:space-between;align-items:center;gap:24px}.brand{font-size:24px;font-weight:700;text-decoration:none;display:flex;align-items:center;gap:12px}.brand svg{width:40px;height:40px}nav{display:flex;gap:24px;flex-wrap:wrap}nav a{font-size:14px;text-decoration:none;min-height:48px;display:flex;align-items:center}.hero{max-width:1120px;padding:76px 32px 56px;margin:auto}h1{font-family:FloraFallback,Georgia,serif;font-weight:400;max-width:850px;font-size:clamp(40px,6vw,76px);line-height:1.08;letter-spacing:-1.8px;margin:0 0 28px}.intro{font-size:20px;max-width:690px;color:#546052}.notice{margin-top:32px;max-width:690px;padding:16px 20px;border-left:3px solid #c8795b;background:#f0e9df;font-size:14px}.content{max-width:1120px;margin:auto;padding:0 32px 64px;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:20px 72px}.content section{padding:20px 0 24px;border-top:1px solid #d9dfd4}h2{font-family:Georgia,serif;font-size:28px;line-height:1.25;font-weight:400}.content p{max-width:600px}footer{background:#e6ebdf;padding:40px 32px;color:#4d5c4c}footer div{max-width:1056px;margin:auto}footer ul{padding:0;list-style:none;display:flex;flex-wrap:wrap;gap:12px 24px}footer a{min-height:48px;display:inline-flex;align-items:center;font-size:14px}small{display:block;max-width:780px} @media(max-width:700px){header{align-items:flex-start;flex-direction:column;padding:20px}nav{gap:18px}.hero{padding:48px 20px 32px}h1{letter-spacing:-1px}.content{padding:0 20px 40px;grid-template-columns:1fr;gap:0}.intro{font-size:18px}}@media(prefers-color-scheme:dark){body{background:#17251d;color:#f6f4ed}a{color:#b7d2b8}.intro{color:#c1cdbd}header,.content section{border-color:#415343}.notice{background:#354033}footer{background:#26382b;color:#c7d2c1}}"""


def validate_origin(value: str) -> str:
    parsed = urlparse(value)
    if parsed.scheme != "https" or not parsed.hostname or parsed.query or parsed.fragment or parsed.username or parsed.password:
        raise ValueError("SITE_URL must be an absolute HTTPS origin/path with no credentials, query or fragment")
    return value.rstrip("/")


def page_path(slug: str) -> str:
    return "index.html" if slug == "index" else f"{slug}.html"


def build(output: Path, site_url: str = "", appstore_url: str = "") -> dict:
    origin = validate_origin(site_url) if site_url else ""
    if appstore_url:
        parsed = urlparse(appstore_url)
        if parsed.scheme != "https" or not re.fullmatch(r"(?:www\.)?amazon\.[a-z.]+", parsed.hostname or "") or "/dp/" not in parsed.path:
            raise ValueError("APPSTORE_URL must be a verified HTTPS Amazon product /dp/ URL")
    output.mkdir(parents=True, exist_ok=True)
    (output / "style.css").write_text(CSS, encoding="utf-8")
    mark = '<svg viewBox="0 0 48 48" aria-hidden="true"><path fill="#315c45" d="M24 3C18 12 8 23 8 32a16 16 0 0 0 32 0C40 23 30 12 24 3Z"/><path fill="#b5c9a6" d="M23 34C11 22 26 17 34 17c0 10-3 17-11 17Z"/><path d="m19 39 12-17" fill="none" stroke="#faf8f2" stroke-width="2"/></svg>'
    nav = '<nav aria-label="Main navigation"><a href="features.html">Features</a><a href="fire-tablet-compatibility.html">Fire tablets</a><a href="pricing.html">Pricing</a><a href="support.html">Support</a></nav>'
    for slug, (heading, description, sections) in PAGES.items():
        path = page_path(slug)
        canonical = f'{origin}/{path}' if origin else ""
        metadata = f'<link rel="canonical" href="{html.escape(canonical)}"><link rel="alternate" hreflang="en-GB" href="{html.escape(canonical)}">' if origin else '<meta name="robots" content="noindex,nofollow">'
        schema = {"@context": "https://schema.org", "@type": "SoftwareApplication", "name": "FloraTend", "applicationCategory": "LifestyleApplication", "operatingSystem": "Android", "description": "Plant care and watering tracker in development. Not yet available for download."}
        if origin:
            schema["url"] = origin + "/index.html"
        body = "".join(f'<section><h2>{html.escape(title)}</h2><p>{html.escape(text)}</p></section>' for title, text in sections)
        footer_links = "".join(f'<li><a href="{page_path(key)}">{html.escape("Home" if key == "index" else key.replace("-", " ").title())}</a></li>' for key in PAGES)
        store = f'<p><a href="{html.escape(appstore_url)}">View FloraTend on Amazon Appstore</a></p>' if appstore_url else ""
        structured_data = json.dumps(schema).replace("<", "\\u003c")
        document = f'<!doctype html><html lang="en-GB"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>{html.escape(heading)} | FloraTend</title><meta name="description" content="{html.escape(description)}">{metadata}<link rel="stylesheet" href="style.css"><script type="application/ld+json">{structured_data}</script></head><body><header><a class="brand" href="index.html">{mark}FloraTend</a>{nav}</header><main id="main"><div class="hero"><h1>{html.escape(heading)}</h1><p class="intro">{html.escape(description)}</p><p class="notice">In development. App availability, supported devices, translations and Premium purchasing await release verification.</p>{store}</div><div class="content">{body}</div></main><footer><div><p>Every plant deserves a little attention.</p><ul>{footer_links}</ul><small>FloraTend is a provisional product name. Care information is general education, not a diagnosis. No claim of universal device or voice compatibility is made.</small></div></footer></body></html>'
        (output / path).write_text(document, encoding="utf-8")
    ET.register_namespace("", "http://www.sitemaps.org/schemas/sitemap/0.9")
    sitemap = ET.Element("{http://www.sitemaps.org/schemas/sitemap/0.9}urlset")
    if origin:
        for slug in PAGES:
            entry = ET.SubElement(sitemap, "{http://www.sitemaps.org/schemas/sitemap/0.9}url")
            ET.SubElement(entry, "{http://www.sitemaps.org/schemas/sitemap/0.9}loc").text = f"{origin}/{page_path(slug)}"
    ET.ElementTree(sitemap).write(output / "sitemap.xml", encoding="utf-8", xml_declaration=True)
    robots = f"User-agent: *\nAllow: /\nSitemap: {origin}/sitemap.xml\n" if origin else "User-agent: *\nDisallow: /\n"
    (output / "robots.txt").write_text(robots, encoding="utf-8")
    report = {"pages": len(PAGES), "language": "en-GB", "production_configured": bool(origin), "canonical_origin": origin or None, "appstore_link_configured": bool(appstore_url), "pending": ["Verified deployment origin required"] if not origin else []}
    report["pending"].extend(["Publisher legal/contact information and final privacy review", "Translated pages and human review", "Product facts reconciliation against verified release", "Verified Appstore listing URL"])
    (output / "build-report.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    return report


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=ROOT / "dist")
    parser.add_argument("--production", action="store_true")
    args = parser.parse_args()
    site_url = os.environ.get("SITE_URL", "")
    if args.production and not site_url:
        parser.error("Production builds require SITE_URL; do not invent a domain")
    print(json.dumps(build(args.output, site_url, os.environ.get("APPSTORE_URL", "")), indent=2))


if __name__ == "__main__":
    main()

