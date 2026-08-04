package me.croabeast.prismatic.element;

/**
 * One styled run of text with its optional click and hover events.
 *
 * <p>The colorized output is cached per {@link Target}. There are only two profiles, so an element
 * rendered to a hundred players pays the color pipeline twice instead of a hundred times. A render
 * that actually resolves placeholders is not cacheable and is computed every time.
 */
final class Segment {

    private static final int TARGETS = Target.values().length;

    final String raw;
    final Part[] parts;
    final Click click;
    final String clickValue;
    final Hover hover;

    private final boolean dynamic;
    private final String[] cache = new String[TARGETS];

    Segment(String raw, Click click, String clickValue, Hover hover) {
        this.raw = raw == null ? "" : raw;
        this.parts = Part.split(this.raw);
        this.click = click;
        this.clickValue = clickValue;
        this.hover = hover;

        boolean dynamic = false;
        for (Part part : parts) {
            if (!part.placeholder) continue;
            dynamic = true;
            break;
        }
        this.dynamic = dynamic;
    }

    Segment(String raw) {
        this(raw, null, null, null);
    }

    Segment withRaw(String raw) {
        return new Segment(raw, click, clickValue, hover);
    }

    Segment withClick(Click click, String clickValue) {
        return new Segment(raw, click, clickValue, hover);
    }

    Segment withHover(Hover hover) {
        return new Segment(raw, click, clickValue, hover);
    }

    boolean hasEvents() {
        return click != null || hover != null;
    }

    /**
     * Returns the colorized text of this segment for the given render.
     */
    String render(RenderContext context) {
        boolean cacheable = context.hasDefaultFormatter() && (!dynamic || !context.hasResolver());
        int slot = context.getTarget().ordinal();

        if (cacheable) {
            String cached = cache[slot];
            if (cached != null) return cached;
        }

        String colored = context.colorize(resolve(context));
        if (cacheable) cache[slot] = colored;

        return colored;
    }

    /**
     * Returns the text of this segment with placeholders resolved but no colors applied.
     */
    String resolve(RenderContext context) {
        if (parts.length == 1 && !parts[0].placeholder) return parts[0].value;

        StringBuilder builder = new StringBuilder(raw.length() + 16);
        for (Part part : parts)
            builder.append(part.placeholder ? context.resolve(part.value, part.literal) : part.value);

        return builder.toString();
    }
}
