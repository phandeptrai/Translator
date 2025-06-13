package com.example.translator.ui.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.translator.model.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(
    languages: List<Language>,
    selectedLanguageCode: String,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null // Thêm giá trị mặc định null cho label
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = languages.find { it.code == selectedLanguageCode }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { if (label != null) Text(label) else null }, // Hiển thị label chỉ khi có giá trị
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.name) },
                    onClick = {
                        onSelectionChange(language.code)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}