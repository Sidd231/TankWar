/**
 * Observer interface to decouple Game state changes from UI components.
 */
public interface GameEventListener {
    void onLivesChanged(int lives);
    void onLevelChanged(int level);
    void onGridToggled(boolean showGrid);
    void onPauseToggled(boolean isPaused);
    void onGameOver();
    void onGameWon();
}
