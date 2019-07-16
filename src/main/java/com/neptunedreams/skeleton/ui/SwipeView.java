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
 * <p>
 * This class is very specific right now, but I hope to generalize it for other special effects later.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/4/18
 * <p>Time: 12:38 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class SwipeView<C extends JComponent> extends LayerUI<C> {
  @SuppressWarnings({"WeakerAccess", "JavaDoc"})
  public static <J extends JComponent> SwipeView<J> wrap(J recordView) {
    JLayer<J> jLayer = new JLayer<>(recordView);
    final SwipeView<J> ui = new SwipeView<>(recordView, jLayer);
    jLayer.setUI(ui);
    return ui;
  }
  
  private final C liveComponent;
  private @Nullable Image priorScreen=null;
  private @Nullable Image upcomingScreen= null;
  private final JLayer<C> layer;
  
  private boolean isAnimating = false;
  private SwipeDirection swipeDirection = SwipeDirection.SWIPE_RIGHT;
  private static final int animationDurationMillis = 500;
  private static final int maxFrames = 15;
  // Calculated:
  @SuppressWarnings("FieldCanBeLocal")
  private static final int frameMillis = animationDurationMillis/maxFrames;
  private int frame = 0;
  
  private SwipeView(C view, JLayer<C> theLayer) {
    super();
    liveComponent = view;
    layer = theLayer;
  }

  @SuppressWarnings("WeakerAccess")
  public JLayer<C> getLayer() { return layer; }

  /**
   * Perform the specified operation with a swipe-right special effect. This is often used in an ActionListener:
   * <pre>
   *   first.addActionListener((e) -> swipeView.swipeRight(recordModel::goFirst));
   * </pre>
   * Here, the Action listener will perform a Swipe-right after executing the goFirst() method of recordModel.
   * @param operation The operation
   */
  @SuppressWarnings("WeakerAccess")
  public void swipeRight(Runnable operation) {
    swipe(operation,SwipeDirection.SWIPE_RIGHT);
  }

  /**
   * Perform the specified operation with a swipe-left special effect. This is often used in an ActionListener:
   * <pre>
   *   first.addActionListener((e) -> swipeView.swipeLeft(recordModel::goFirst));
   * </pre>
   * Here, the Action listener will perform a Swipe-Left after executing the goFirst() method of recordModel.
   *
   * @param operation The operation
   */
  @SuppressWarnings("WeakerAccess")
  public void swipeLeft(Runnable operation) {
    swipe(operation, SwipeDirection.SWIPE_LEFT);
  }

  private void swipe(Runnable operation, SwipeDirection swipeDirection) {
    prepareToAnimate(swipeDirection);
    operation.run();
    animate();
  }

  @Override
  public void paint(final Graphics g, final JComponent c) {
    if (isAnimating) {
      int xLimit = (c.getWidth() * frame) / maxFrames;
      if (swipeDirection == SwipeDirection.SWIPE_LEFT) {
        xLimit = c.getWidth() - xLimit;
      }
      int width = c.getWidth();
      int height = c.getHeight();

      assert upcomingScreen != null;
      assert priorScreen != null;
      Image pScreen = Objects.requireNonNull(priorScreen);
      if (swipeDirection == SwipeDirection.SWIPE_RIGHT) {
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
  
  private void prepareToAnimate(SwipeDirection swipeDirection) {
    this.swipeDirection = swipeDirection;
    isAnimating = true;
    frame = 0;

    // Save current state
    priorScreen = new BufferedImage(liveComponent.getWidth(), liveComponent.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2D = (Graphics2D) priorScreen.getGraphics();
    liveComponent.paint(graphics2D);
    graphics2D.dispose();
  }

  private void animate() {
    @SuppressWarnings("argument.type.incompatible")    // Stub this out!
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
    upcomingScreen = new BufferedImage(liveComponent.getWidth(), liveComponent.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2D = (Graphics2D) upcomingScreen.getGraphics();
    liveComponent.paint(graphics2D);
    graphics2D.dispose();
    
    timer.start();
  }

  /**
   * This lets you assign an action to a button that executes on mouseStillDown, but only when animation has completed.
   * This lets the user, say, hold an arrow button down and watch it page through the entries, animating each new page.
   * This method effectively replaces a call to addActionListener. Don't use that method if you're using this one.
   * <p>
   * Todo: Add Keystroke tracking
   * @param button The button to apply the mouseDown action to
   * @param operation The code to execute when the mouse is down.
   * @param swipeRight True for swipeRight, false for swipe left
   */
  @SuppressWarnings("WeakerAccess")
  public void assignMouseDownAction(AbstractButton button, Runnable operation, SwipeDirection swipeRight) {
    MouseTracker mouseTracker = new MouseTracker(operation, swipeRight);
    button.addMouseListener(mouseTracker);
  }
  
  private class MouseTracker extends MouseAdapter {
    private boolean active = false;
    private boolean tracking = false;
    @SuppressWarnings("argument.type.incompatible") // Stub this out!
    private final Timer timer = new Timer(frameMillis, null);

    MouseTracker(Runnable operation, SwipeDirection swipeDirection) {
      super();
      ActionListener listener = (e) -> {
        if (active && ! isAnimating) {
          swipe(operation, swipeDirection);
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
