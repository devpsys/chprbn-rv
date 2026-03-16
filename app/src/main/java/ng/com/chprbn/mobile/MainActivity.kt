package ng.com.chprbn.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton
import ng.com.chprbn.mobile.core.designsystem.components.Status
import ng.com.chprbn.mobile.core.designsystem.components.StatusBadge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChprbnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DesignSystemPreviewScreen()
                }
            }
        }
    }
}

@Composable
private fun DesignSystemPreviewScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CHPRBN Design System",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Buttons & status badges preview",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        PrimaryButton(
            text = "Primary action",
            onClick = { /* no-op sample */ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatusBadge(status = Status.Active)
        StatusBadge(status = Status.Pending)
        StatusBadge(status = Status.Expired)
    }
}
