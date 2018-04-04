package com.neptunedreams.skeleton.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.LayerUI;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * SwipeView adds a swipe special effect to a Component. This draws a swipe-right or swipe-left effect on a chosen 
 * action. It also optionally supports a repeated action when the mouse is held down.
 * <p/>
 * This class is very specific right now, but I hope to generalize it for other special effects later.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/4/18
 * <p>Time: 12:38 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class SwipeView<R> extends LayerUI<RecordView<R>> {
@SuppressWarnings("WeakerAccess")
public static <R> SwipeView<R> wrap(RecordView<R> recordView) {
    JLayer<RecordView<R>> jLayer = new JLayer<>(recordView);
    final SwipeView<R> ui = new SwipeView<>(recordView, jLayer);
    jLayer.setUI(ui);
    return ui;
  }
  
  private final RecordView<R> recordView;
  private @Nullable Image priorScreen=null;
  private @Nullable Image upcomingScreen= null;
  private final JLayer<RecordView<R>> layer;
  
  private boolean isAnimating = false;
  private boolean swipeRight = true;
  private static final int animationDurationMillis = 500;
  private static final int maxFrames = 15;
  // Calculated:
  @SuppressWarnings("FieldCanBeLocal")
  private static final int frameMillis = animationDurationMillis/maxFrames;
  private int frame = 0;
  
  private SwipeView(RecordView<R> view, JLayer<RecordView<R>> theLayer) {
    super();
    recordView = view;
    layer = theLayer;
  }

  @SuppressWarnings("WeakerAccess")
  public JLayer<RecordView<R>> getLayer() { return layer; }

  @SuppressWarnings("WeakerAccess")
  public void swipeRight(Runnable operation) {
    swipe(operation,true);
  }

  @SuppressWarnings("WeakerAccess")
  public void swipeLeft(Runnable operation) {
    swipe(operation, false);
  }

  private void swipe(Runnable operation, boolean goRight) {
    prepareToAnimate(goRight);
    operation.run();
    animate();
  }

  @Override
  public void paint(final Graphics g, final JComponent c) {
    if (isAnimating) {
      int xLimit = (c.getWidth() * frame) / maxFrames;
      if (!swipeRight) {
        xLimit = c.getWidth() - xLimit;
      }
      int width = c.getWidth();
      int height = c.getHeight();

      assert upcomingScreen != null;
      assert priorScreen != null;
      Image pScreen = Objects.requireNonNull(priorScreen);
      if (swipeRight) {
        g.drawImage(upcomingScreen, 0, 0, xLimit, height, 0, 0, xLimit, height, c);
        g.drawImage(pScreen, xLimit, 0, width, height, xLimit, 0, width, height, c);
      } else {
        g.drawImage(upcomingScreen, xLimit, 0, width, height, xLimit, 0, width, height, c);
        g.drawImage(pScreen, 0, 0, xLimit, height, 0, 0, xLimit, height, c);
      }
    } else {
      super.paint(g, c);
    }
  }
  
  private void prepareToAnimate(boolean goRight) {
    swipeRight = goRight;
    isAnimating = true;
    frame = 0;

    // Save current state
    priorScreen = new BufferedImage(recordView.getWidth(), recordView.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2D = (Graphics2D) priorScreen.getGraphics();
    recordView.paint(graphics2D);
    graphics2D.dispose();
  }
  
  private void animate() {
    @SuppressWarnings("argument.type.incompatible")
    Timer timer = new Timer(frameMillis, null);
    final ActionListener actionListener = (evt) -> {
      frame++;
      layer.repaint();
      if (frame == maxFrames) {
        frame = 0;
        isAnimating = false;
        timer.stop();
      }
    };
    timer.addActionListener(actionListener);
    upcomingScreen = new BufferedImage(recordView.getWidth(), recordView.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2D = (Graphics2D) upcomingScreen.getGraphics();
    recordView.paint(graphics2D);
    graphics2D.dispose();
    
    timer.start();
  }

  /**
   * This lets you assign an action to a button that executes on mouseStillDown, but only when animation has completed.
   * This lets the user, say, hold an arrow button down and watch it page through the entries, animating each new page.
   * This method effectively replaces a call to addActionListener. Don't use that method if you're using this one.
   * @param button The button to apply the mouseDown action to
   * @param operation The code to execute when the mouse is down.
   */
  @SuppressWarnings("WeakerAccess")
  public void assignMouseDownAction(AbstractButton button, Runnable operation, boolean goRight) {
    MouseTracker mouseTracker = new MouseTracker(operation, goRight);
    button.addMouseListener(mouseTracker);
  }
  
  private class MouseTracker extends MouseAdapter {
    private boolean active = false;
    private boolean tracking = false;
    @SuppressWarnings("argument.type.incompatible")
    private final Timer timer = new Timer(frameMillis, null);

    MouseTracker(Runnable operation, boolean goRight) {
      super();
      ActionListener listener = (e) -> {
        if (active && ! isAnimating) {
          swipe(operation, goRight);
        }
      };
      timer.addActionListener(listener);
    }

    @Override
    public void mousePressed(final MouseEvent e) {
      if (SwingUtilities.isLeftMouseButton(e)) {
        active = true;
        tracking = true;
        timer.start();
      }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
      if (tracking) {
        active = false;
        timer.stop();
        tracking = false;
      }
    }

    @Override
    public void mouseExited(final MouseEvent e) {
      if (tracking) {
        active = false;
      }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
      if (tracking) {
        active = true;
      }
    }
  }
}
